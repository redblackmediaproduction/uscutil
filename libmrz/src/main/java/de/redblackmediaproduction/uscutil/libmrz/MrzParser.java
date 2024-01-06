package de.redblackmediaproduction.uscutil.libmrz;

import de.redblackmediaproduction.uscutil.libmrz.exception.InvalidMrzException;
import de.redblackmediaproduction.uscutil.libmrz.mrz.ParsedMrz;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;

public class MrzParser {
    private static final Logger logger = LogManager.getLogger(MrzParser.class);

    /**
     * ICAO 9303 Type 3, 2x44
     * <p>
     * See <a href="https://en.wikipedia.org/wiki/Machine-readable_passport#Passport_booklets">Wikipedia</a>
     * </p>
     *
     * @param line1 First line
     * @param line2 Second line
     */
    public static ParsedMrz fromType3(String line1, String line2) throws InvalidMrzException, FileNotFoundException {
        //Validate lengths
        if (line1.length() != 44)
            throw new InvalidMrzException("Line 1 length is not 44 characters");
        if (line2.length() != 44)
            throw new InvalidMrzException("Line 2 length is not 44 characters");
        ParsedMrz ret = new ParsedMrz();
        ret.setFormat(ParsedMrz.FORMATS.TYPE3);
        ret.setLines(line1, line2);
        return ret;
    }

    /**
     * ICAO 9303 Type 2, 2x36
     * <p>
     * See <a href="https://en.wikipedia.org/wiki/Machine-readable_passport#Official_travel_documents">Wikipedia</a>
     * </p>
     *
     * @param line1 First line
     * @param line2 Second line
     */
    public static ParsedMrz fromType2(String line1, String line2) throws InvalidMrzException, FileNotFoundException {
        //Validate lengths
        if (line1.length() != 36)
            throw new InvalidMrzException("Line 1 length is not 36 characters");
        if (line2.length() != 36)
            throw new InvalidMrzException("Line 2 length is not 36 characters");
        ParsedMrz ret = new ParsedMrz();
        ret.setFormat(ParsedMrz.FORMATS.TYPE2);
        ret.setLines(line1, line2);
        return ret;
    }

    /**
     * ICAO 9303 Type 1, 3x30
     * <p>
     * See <a href="https://en.wikipedia.org/wiki/Machine-readable_passport#Official_travel_documents">Wikipedia</a>
     * </p>
     *
     * @param line1 First line
     * @param line2 Second line
     * @param line3 Third line
     */
    public static ParsedMrz fromType1(String line1, String line2, String line3) throws InvalidMrzException, FileNotFoundException {
        //Validate lengths
        if (line1.length() != 30)
            throw new InvalidMrzException("Line 1 length is not 30 characters");
        if (line2.length() != 30)
            throw new InvalidMrzException("Line 2 length is not 30 characters");
        if (line3.length() != 30)
            throw new InvalidMrzException("Line 3 length is not 30 characters");
        ParsedMrz ret = new ParsedMrz();
        ret.setFormat(ParsedMrz.FORMATS.TYPE1);
        ret.setLines(line1, line2, line3);
        return ret;
    }
}
