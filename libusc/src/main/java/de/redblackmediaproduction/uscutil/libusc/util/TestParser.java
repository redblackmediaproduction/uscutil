package de.redblackmediaproduction.uscutil.libusc.util;

import de.redblackmediaproduction.uscutil.libusc.atr.AtrParser;
import de.redblackmediaproduction.uscutil.libusc.atr.ParsedAtr;

public class TestParser {
    public static void main(String[] args) {
        try {
            String[] inputs = {
                    "3b 87 80 01 80 31 c0 73 d6 31 c0 23",
                    "3b ff 18 00 ff 81 31 fe 45 65 63 06 08 71 02 50 00 23 b8 a0 65 40 47 11 70",
            };
            for (String input : inputs) {
                System.out.println("Input: " + input);
                ParsedAtr atr = AtrParser.fromString(input);
                System.out.println("Result:" + atr.toString());
            }
        } catch (Exception e) {
            System.err.println("Caught " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}