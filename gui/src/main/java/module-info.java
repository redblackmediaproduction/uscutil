module de.redblackmediaproduction.uscutil {
    exports de.redblackmediaproduction.uscutil.gui;
    exports de.redblackmediaproduction.uscutil.gui.cameraview;
    requires java.smartcardio;
    requires de.redblackmediaproduction.uscutil.libusc;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires webcam.capture;
    requires webcam.capture.driver.opencv;
    requires de.redblackmediaproduction.uscutil.libmrz;
    requires org.bytedeco.javacv;
    requires de.redblackmediaproduction.uscutil.libmrz_cv;
    opens de.redblackmediaproduction.uscutil.gui.cameraview;
}