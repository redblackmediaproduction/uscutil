package atr;

import de.redblackmediaproduction.uscutil.libusc.atr.AtrParser;
import de.redblackmediaproduction.uscutil.libusc.exception.InvalidAtrException;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ImportTests {
    @Test
    void testSpaceSeparator() {
        String input = "3b 87 80 01 80 31 b8 73 86 01 e0 1b";
        byte[] ret = new byte[0];
        try {
            ret = AtrParser.fromString(input).getAtr().getBytes();
        } catch (InvalidAtrException ignored) {
        }
        assertEquals(input, HexFormat.ofDelimiter(" ").formatHex(ret));
    }

    @Test
    void testColonSeparator() {
        String input = "3b:87:80:01:80:31:b8:73:86:01:e0:1b";
        byte[] ret = new byte[0];
        try {
            ret = AtrParser.fromString(input).getAtr().getBytes();
        } catch (InvalidAtrException ignored) {
        }
        assertEquals(input, HexFormat.ofDelimiter(":").formatHex(ret));
    }

    @Test
    void testNoSeparator() {
        String input = "3b8780018031b8738601e01b";
        byte[] ret = new byte[0];
        try {
            ret = AtrParser.fromString(input).getAtr().getBytes();
        } catch (InvalidAtrException ignored) {
        }
        assertEquals(input, HexFormat.of().formatHex(ret));
    }

    @Test
    void testUppercase() {
        String input = "3b8780018031b8738601e01b".toUpperCase();
        byte[] ret = new byte[0];
        try {
            ret = AtrParser.fromString(input).getAtr().getBytes();
        } catch (InvalidAtrException ignored) {
        }
        assertEquals(input, HexFormat.of().formatHex(ret).toUpperCase());
    }

    @Test
    void testCutoff() {
        String input = "3b8780018031b8738601e01";
        assertThrows(InvalidAtrException.class, () -> {
            AtrParser.fromString(input);
        });
    }

    @Test
    void testRaw() {
        String input = "\u003b\u0087\u0080\u0001\u0080\u0031\u00b8\u0073\u0086\u0001\u00e0\u001b";
        byte[] ret = new byte[0];
        try {
            ret = AtrParser.fromString(input).getAtr().getBytes();
        } catch (InvalidAtrException ignored) {
        }
        assertEquals("3b 87 80 01 80 31 b8 73 86 01 e0 1b", HexFormat.ofDelimiter(" ").formatHex(ret));
    }
}
