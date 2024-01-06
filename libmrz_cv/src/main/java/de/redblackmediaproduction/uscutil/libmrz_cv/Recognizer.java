package de.redblackmediaproduction.uscutil.libmrz_cv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.LeptonicaFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.tesseract.TessBaseAPI;
import org.bytedeco.tesseract.global.tesseract;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_photo.fastNlMeansDenoising;

/**
 * Entrypoint class that takes an image of an ICAO 9303-compliant ID card and attempts to extract the MRZ out of it
 */
public class Recognizer {
    private static final Logger logger = LogManager.getLogger(Recognizer.class);

    /**
     * Auto-crop an image - ideally, the ID card should be the only (dominant) thing on the picture
     *
     * @param mat
     * @param imagePath
     */
    protected static Mat cropImage(Mat mat, String imagePath) {
        int tempIndex = 0;
        Mat originalImage = mat.clone();

        //Denoise
        fastNlMeansDenoising(mat, mat, 3, 7, 21);
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_post_denoise.jpg", mat);

        //Resize to some sort of standard for faster processing
        int targetWidth = 800;
        int newHeight = Math.toIntExact(Math.round(((double) mat.size().height() / mat.size().width()) * targetWidth));
        double scaleFactor = (double) targetWidth / mat.size().width();
        logger.trace(String.format("Old width %d, new width %d, scale factor %f, old height %d, new height %d%n", mat.size().width(), targetWidth, scaleFactor, mat.size().height(), newHeight));
        resize(mat, mat, new Size(targetWidth, newHeight));
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_post_resize.jpg", mat);

        //Transform to grayscale
        cvtColor(mat, mat, COLOR_RGB2GRAY);
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_post_gray.jpg", mat);

        //Transform to black-white
        //Threshold values from https://www.projectpro.io/recipes/what-is-dilation-of-image-dilate-image-opencv
        threshold(mat, mat, 155, 255, THRESH_BINARY_INV);
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_post_threshold.jpg", mat);

        //Dilate to make life easier for the edge detector
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(10, 10));
        dilate(mat, mat, kernel);
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_post_dilate.jpg", mat);

        //Detect edges
        double threshold = 3.0;
        Mat edges = new Mat();
        Canny(mat, edges, threshold, 3 * threshold, 3, false);
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_edges.jpg", edges);

        //Blow up the edge map again a bit
        dilate(edges, edges, kernel);
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_edges_post_dilate.jpg", edges);

        //Now, run a contour detector, and try to auto-crop.
        MatVector contours = new MatVector();
        findContours(edges, contours, new Mat(), RETR_LIST, CHAIN_APPROX_SIMPLE);

        //For all countour boxes, check if the bounding box contains them. If not, expand the bounding box
        int[] boundingBox = {mat.size().width(), mat.size().height(), 0, 0};
        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            Rect contourBb = boundingRect(contour);
            logger.trace(String.format("Bounding box: anchor at (%d,%d), %dx%d", contourBb.x(), contourBb.y(), contourBb.width(), contourBb.height()));
            if (contourBb.x() < boundingBox[0])
                boundingBox[0] = contourBb.x();
            if (contourBb.y() < boundingBox[1])
                boundingBox[1] = contourBb.y();
            if (contourBb.x() + contourBb.width() > boundingBox[2])
                boundingBox[2] = contourBb.x() + contourBb.width();
            if (contourBb.y() + contourBb.height() > boundingBox[3])
                boundingBox[3] = contourBb.y() + contourBb.height();

        }
        //Now, add 2% of the box's width/height as "safety margin"
        logger.trace(String.format("Final bounding box: p1(%d,%d)-p2(%d,%d)", boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
        //Calculate the width and height
        int bbW = boundingBox[2] - boundingBox[0];
        int bbH = boundingBox[3] - boundingBox[1];
        double bbMargin = 0.02;
        boundingBox[0] = boundingBox[0] - ((int) Math.round(bbW * bbMargin));
        if (boundingBox[0] < 0)
            boundingBox[0] = 0;
        boundingBox[1] = boundingBox[1] - ((int) Math.round(bbH * bbMargin));
        if (boundingBox[1] < 0)
            boundingBox[1] = 0;
        boundingBox[2] = boundingBox[2] + ((int) Math.round(bbW * bbMargin));
        if (boundingBox[2] > mat.size().width())
            boundingBox[2] = mat.size().width();
        boundingBox[3] = boundingBox[3] + ((int) Math.round(bbH * bbMargin));
        if (boundingBox[3] > mat.size().height())
            boundingBox[3] = mat.size().height();
        logger.trace(String.format("Bounding box with margin: p1(%d,%d)-p2(%d,%d)", boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
        rectangle(mat, new Point(boundingBox[0], boundingBox[1]), new Point(boundingBox[2], boundingBox[3]), new Scalar(255, 0, 0, 0));
        //Save annotated image
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_annotated.jpg", mat);
        //Scale the bounding box to original mat
        int[] scaledBoundingBox = {
                Math.toIntExact(Math.round(boundingBox[0] / scaleFactor)),
                Math.toIntExact(Math.round(boundingBox[1] / scaleFactor)),
                Math.toIntExact(Math.round(boundingBox[2] / scaleFactor)),
                Math.toIntExact(Math.round(boundingBox[3] / scaleFactor)),
        };
        logger.trace(String.format("Scaled bounding box: p1(%d,%d)-p2(%d,%d)", boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
        //and extract the suspected ID card from the picture
        Mat croppedOriginal = new Mat(originalImage, new Rect(new Point(scaledBoundingBox[0], scaledBoundingBox[1]), new Point(scaledBoundingBox[2], scaledBoundingBox[3])));
        imwrite(imagePath + "_crop_" + (tempIndex++) + "_final.jpg", croppedOriginal);

        return croppedOriginal;
    }

    /**
     * Prepare an image for OCR (convert to black-white, denoise, sharpen)
     *
     * @param mat
     * @param imagePath
     * @return
     */
    private static Mat prepareOcr(Mat mat, String imagePath) {
        int tempIndex = 0;

        //Transform to grayscale
        cvtColor(mat, mat, COLOR_RGB2GRAY);
        imwrite(imagePath + "_prepare_" + (tempIndex++) + "_post_gray.jpg", mat);

        //Transform to black-white
        //Threshold values from https://www.projectpro.io/recipes/what-is-dilation-of-image-dilate-image-opencv
        threshold(mat, mat, 155, 255, THRESH_BINARY_INV);
        imwrite(imagePath + "_prepare_" + (tempIndex++) + "_post_threshold.jpg", mat);

        //Imgproc.morphologyEx(mat,mat,Imgproc.MORPH_CLOSE,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3)));
        erode(mat, mat, getStructuringElement(MORPH_RECT, new Size(2, 2)));
        imwrite(imagePath + "_prepare_" + (tempIndex++) + "_post_denoise.jpg", mat);

        return mat;
    }

    /**
     * Attempt to extract the MRZ out of an image file
     *
     * @param imagePath
     * @return
     */
    public static String[] readMrz(String imagePath) {
        //Load image
        Mat originalImage = imread(imagePath);
        return readMrz(originalImage, imagePath);
    }

    /**
     * Attempt to extract the MRZ out of an OpenCV Mat representation of an image
     *
     * @param originalImage
     * @return
     */
    public static String[] readMrz(Mat originalImage) {
        //We're probably being called from the GUI, fake a file name for the debugger
        String imagePath = System.getProperty("user.dir") + "/stream.jpg";
        return readMrz(originalImage, imagePath);
    }

    /**
     * Internal method actually doing the extraction
     *
     * @param originalImage
     * @param imagePath
     * @return
     */
    protected static String[] readMrz(Mat originalImage, String imagePath) {
        String[] ret = {};

        //First, crop the image (especially important if using a scanner to acquire the image)
        Mat croppedImage = cropImage(originalImage, imagePath);
        //Now, prepare the image in a way suitable for OCR
        Mat ocrImage = prepareOcr(croppedImage, imagePath);

        //Follow https://github.com/piersy/BasicTesseractExample/blob/master/src/test/java/BasicTesseractExampleTest.java
        TessBaseAPI api = new TessBaseAPI();
        api.Init(
                Recognizer.class.getClassLoader().getResource("tessdata_fast").getPath(),
                "eng"
        );
        api.SetPageSegMode(tesseract.PSM_AUTO);
        LeptonicaFrameConverter lfc = new LeptonicaFrameConverter();
        OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
        Frame ocrFrame = matConverter.convert(ocrImage);

        api.SetImage(lfc.convert(ocrFrame));
        String ocrReturn = api.GetUTF8Text().getString();

        logger.trace(String.format("Raw OCR return: '%s'", ocrReturn));

        String[] lines = ocrReturn.split("\n");
        for (String line : lines) {
            if (line.trim().matches("^[0-9A-Z<]{44}$") //ICAO 9303-3
                    || line.trim().matches("^[0-9A-Z<]{36}$") //ICAO 9303-2
                    || line.trim().matches("^[0-9A-Z<]{30}$")) {  //ICAO 9303-1
                logger.trace(String.format("Appending line '%s' to return"));
                ret = org.apache.commons.lang3.ArrayUtils.add(ret, line);
            }
        }

/*
        OCRTesseract tesseractEngine= OCRTesseract.create(
                Recognizer.class.getClassLoader().getResource("tessdata_fast").getPath(),
                "eng",
                "ABCDEFGHIJKLMNOPQRSTUVWYZ0123456789<",
                opencv_text.OEM_DEFAULT,
                opencv_text.PSM_AUTO_OSD
        );
        System.out.println(tesseractEngine.run(ocrImage,1));
 */
        return ret;
    }

}
