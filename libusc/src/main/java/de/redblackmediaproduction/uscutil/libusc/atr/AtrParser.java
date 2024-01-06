package de.redblackmediaproduction.uscutil.libusc.atr;

import de.redblackmediaproduction.uscutil.libusc.exception.InvalidAtrException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.ATR;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class AtrParser {
    private static final Logger logger = LogManager.getLogger(AtrParser.class);

    /**
     * Parse an ATR string
     *
     * @param input the ATR string. Can be hex with or without separators (space, colon), or a raw byte string
     * @return ParsedAtr instance representing the ATR
     */
    public static ParsedAtr fromString(String input) throws InvalidAtrException {
        byte[] atrContent = normalize(input);
        ATR atr = new ATR(atrContent);
        return fromAtr(atr);
    }

    public static ParsedAtr fromAtr(ATR atr) throws InvalidAtrException {
        ParsedAtr ret = new ParsedAtr();
        ret.setAtr(atr);
        return ret;
    }

    protected static byte[] normalize(String input) throws InvalidAtrException {
        //Check if it matches the structure of a hex-as-string string
        if (input.matches("^([0-9a-fA-F]{2}[: ]?)*$")) {
            //Remove separator, if present, and force-lowercase
            input = input.replaceAll("[: ]", "").toLowerCase();
            HexFormat hf = HexFormat.of();
            return hf.parseHex(input);
        } else if (input.matches("^[0-9a-fA-F: ]*$")) { // Looks like hex-as-string but doesn't pass the length check (e.g. "01 0")
            throw new InvalidAtrException("Failed to parse: looks like hex-as-string but has length mismatch");
        } else { //Raw string
            return input.getBytes(StandardCharsets.ISO_8859_1);
        }
    }
}
