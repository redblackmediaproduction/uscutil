module de.redblackmediaproduction.uscutil.libmrz_cv {
    exports de.redblackmediaproduction.uscutil.libmrz_cv;
    exports de.redblackmediaproduction.uscutil.libmrz_cv.util;
    requires java.management;
    requires de.redblackmediaproduction.uscutil.libmrz;
    requires org.apache.logging.log4j;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires org.bytedeco.tesseract;
    requires org.apache.commons.lang3;
}