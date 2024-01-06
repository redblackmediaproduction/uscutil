package de.redblackmediaproduction.uscutil.libmrz.util;


import de.redblackmediaproduction.uscutil.libmrz.MrzParser;
import de.redblackmediaproduction.uscutil.libmrz.mrz.ParsedMrz;

public class TestParser {
    public static void main(String[] args) {
        try {
            String[][] type3Inputs = {
                    { //DE/Reisepass.jpg
                            "P<D<<MUSTERMANN<<ERIKA<<<<<<<<<<<<<<<<<<<<<<",
                            "C01X00T478D<<6408125F2702283<<<<<<<<<<<<<<<4",
                    },
                    { // ICAO 9303-4, https://www.icao.int/publications/Documents/9303_p4_cons_en.pdf
                            "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<",
                            "L898902C36UTO7408122F1204159ZE184226B<<<<<10",
                    },
                    { // ICAO 9303-7, https://www.icao.int/publications/Documents/9303_p7_cons_en.pdf
                            "V<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<",
                            "L898902C<3UTO6908061F9406236ZE184226B<<<<<<<",
                    }
            };
            for (String[] input : type3Inputs) {
                System.out.println("Input: \nline1:\t" + input[0] + "\nline2:\t" + input[1]);
                ParsedMrz mrz = MrzParser.fromType3(input[0], input[1]);
                System.out.println("Result:" + mrz.toString());
            }
            String[][] type2Inputs = {
                    { //DE/Personalausweis_alt_Vorderseite.jpg
                            "IDD<<MUSTERMANN<<ERIKA<<<<<<<<<<<<<<",
                            "1220010855D<<6408125<1110078<<<<<<<6",
                    },
                    { // ICAO 9303-6, https://www.icao.int/publications/Documents/9303_p6_cons_en.pdf
                            "I<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<",
                            "D231458907UTO7408122F1204159<<<<<<<6",
                    },
                    {// ICAO 9303-7, https://www.icao.int/publications/Documents/9303_p7_cons_en.pdf
                            "V<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<",
                            "L898902C<3UTO6908061F9406236ZE184226",
                    }

            };
            for (String[] input : type2Inputs) {
                System.out.println("Input: \nline1:\t" + input[0] + "\nline2:\t" + input[1]);
                ParsedMrz mrz = MrzParser.fromType2(input[0], input[1]);
                System.out.println("Result:" + mrz.toString());
            }
            String[][] type1Inputs = {
                    { //DE/Personalausweis_Vorderseite.jpg
                            "IDD<<LZ6311T475<<<<<<<<<<<<<<<",
                            "8308126<3110315D<<2108<<<<<<<9",
                            "MUSTERMANN<<ERIKA<<<<<<<<<<<<<",
                    },
                    { // ICAO 9303-5, https://www.icao.int/publications/Documents/9303_p5_cons_en.pdf
                            "I<UTOD231458907<<<<<<<<<<<<<<<",
                            "7408122F1204159UTO<<<<<<<<<<<6",
                            "ERIKSSON<<ANNA<MARIA<<<<<<<<<<",
                    },
                    { // DE/Crew_Member_Certificate.jpg
                            "ACD<<S000000017DLHBS<<<<<<<<<<",
                            "6408125F2012319FRAFAT<<<<<<<<6",
                            "MUSTERMANN<<ERIKA<<<<<<<<<<<<<",
                    }
            };
            for (String[] input : type1Inputs) {
                System.out.println("Input: \nline1:\t" + input[0] + "\nline2:\t" + input[1] + "\nline3:\t" + input[2]);
                ParsedMrz mrz = MrzParser.fromType1(input[0], input[1], input[2]);
                System.out.println("Result:" + mrz.toString());
            }
        } catch (Exception e) {
            System.err.println("Caught " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}