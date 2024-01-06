package de.redblackmediaproduction.uscutil.libmrz_cv.util;

import de.redblackmediaproduction.uscutil.libmrz_cv.Recognizer;

import java.lang.management.ManagementFactory;

public class TestRecognizer {

    public static void main(String[] args) {
        System.out.println("Initializing");
        System.out.println(ManagementFactory.getRuntimeMXBean().getClassPath());

        try {
            Recognizer recognizer = new Recognizer();
            String[] mrz = recognizer.readMrz("/Users/marcoschuster/Projects/uscutil/libmrz_cv/src/test/resources/DE/IDD_Personalausweis_Rueckseite_scan.jpg");
            for (String line : mrz) {
                System.out.println(String.format("Recognized line: '%s' (%d chars)", line, line.length()));
            }
        } catch (Exception e) {
            System.err.println("Caught " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        System.exit(0);
    }
}
