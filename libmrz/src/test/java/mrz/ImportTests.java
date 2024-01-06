package mrz;

import de.redblackmediaproduction.uscutil.libmrz.MrzParser;
import de.redblackmediaproduction.uscutil.libmrz.exception.InvalidMrzException;
import de.redblackmediaproduction.uscutil.libmrz.mrz.ParsedMrz;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ImportTests {
    @Test
    void testType3Input() {
        String[] input = { //DE/Reisepass.jpg
                "P<D<<MUSTERMANN<<ERIKA<<<<<<<<<<<<<<<<<<<<<<",
                "C01X001T478D<<6408125F2702283<<<<<<<<<<<<<<4"
        };
        ParsedMrz ret = null;
        try {
            ret = MrzParser.fromType3(input[0], input[1]);
        } catch (InvalidMrzException | FileNotFoundException ignored) {
        }
        assertNotNull(ret);
        assertNotNull(ret.getLine1());
        assertNotNull(ret.getLine2());
        assertEquals(input[0], ret.getLine1());
        assertEquals(input[1], ret.getLine2());
    }
}
