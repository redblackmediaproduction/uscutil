package de.redblackmediaproduction.uscutil.libusc.atr;

import de.redblackmediaproduction.uscutil.libusc.exception.InvalidAtrException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.ATR;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;

public class ParsedAtr {
    private static final Logger logger = LogManager.getLogger(ParsedAtr.class);
    protected ATR atr = null;
    protected CONVENTION convention = null;
    protected int historicalByteLength = 0;
    protected byte[] historicalBytes = {};
    LinkedHashMap<String, Byte> interfaceCharacters = new LinkedHashMap<>();

    public ATR getAtr() {
        return atr;
    }

    protected void setAtr(ATR atr) throws InvalidAtrException {
        this.atr = atr;
        byte[] rawBytes = atr.getBytes();
        logger.trace("Raw bytes: " + HexFormat.ofDelimiter(" ").formatHex(rawBytes));

        int currentIndex = 0;
        //TS byte. Direct => bit 1 = high voltage, bit 0 = low voltage, Inverse => bit 1 = low voltage, bit 0 = high voltage
        switch (rawBytes[currentIndex]) {
            case (byte) 0x3b:
                this.convention = CONVENTION.DIRECT;
                break;
            case (byte) 0x3f:
                this.convention = CONVENTION.INVERSE;
                break;
            default:
                throw new InvalidAtrException("Invalid convention");
        }
        logger.trace("TS byte: Convention " + (this.convention == CONVENTION.DIRECT ? "DIRECT" : "INVERSE"));
        currentIndex++;

        int icGroup = 1;
        //if any of the icGroups indicates support for T=1 (or above), we will have a TCK checksum byte at the end
        boolean haveT1Protocol = false;
        while (currentIndex < rawBytes.length) {
            boolean taPresent = (rawBytes[currentIndex] & 0b00010000) == 0b00010000;
            boolean tbPresent = (rawBytes[currentIndex] & 0b00100000) == 0b00100000;
            boolean tcPresent = (rawBytes[currentIndex] & 0b01000000) == 0b01000000;
            boolean tdPresent = (rawBytes[currentIndex] & 0b10000000) == 0b10000000;
            int payload = (rawBytes[currentIndex] & 0b00001111);
            logger.trace(String.format("Byte %d (%x) is td%d: ta%d present: %b, tb%d present: %b, tc%d present: %b, td%d present: %b, payload: %x",
                    currentIndex, rawBytes[currentIndex], icGroup - 1, icGroup, taPresent, icGroup, tbPresent, icGroup, tcPresent, icGroup, tdPresent, payload));
            if (icGroup == 1) {
                //T0 byte. Details how long the ATR is in total
                historicalByteLength = payload;
                logger.trace(String.format("Got historical byte length: %d", historicalByteLength));
            } else {
                //TD(icGroup-1) byte, protocol version used for current block
                int protocolVersion = payload;
                logger.trace(String.format("Protocol version: %d", protocolVersion));
                if (protocolVersion > 0)
                    haveT1Protocol = true;
            }
            currentIndex++;
            if (taPresent) {
                logger.trace(String.format("Byte %d (%x) is ta%d", currentIndex, rawBytes[currentIndex], icGroup));
                currentIndex++;
            }
            if (tbPresent) {
                logger.trace(String.format("Byte %d (%x) is tb%d", currentIndex, rawBytes[currentIndex], icGroup));
                currentIndex++;
            }
            if (tcPresent) {
                logger.trace(String.format("Byte %d (%x) is tc%d", currentIndex, rawBytes[currentIndex], icGroup));
                currentIndex++;
            }
            if (tdPresent) {
                icGroup++;
            } else {
                logger.trace("That's it");
                break;
            }
        }
        logger.trace(String.format("current: %d, max %d", currentIndex, rawBytes.length));
        //Check if the ATR is complete, cut-off or carries extra data beyond
        if (currentIndex + historicalByteLength + (haveT1Protocol ? 1 : 0) != rawBytes.length) {
            throw new InvalidAtrException("Length does not match");
        }
        historicalBytes = Arrays.copyOfRange(atr.getBytes(), currentIndex, (currentIndex - 1) + historicalByteLength + (haveT1Protocol ? 1 : 0));
        if (haveT1Protocol) {
            int checksum = 0x0;
            for (int i = 1; i < rawBytes.length - 1; i++) {
                byte v = rawBytes[i];
                //logger.trace(String.format("Checksumming byte %d: %x",i,v));
                checksum = checksum ^ v;
            }
            logger.trace(String.format("TCK %x, got %x", rawBytes[rawBytes.length - 1], checksum));
            if (rawBytes[rawBytes.length - 1] != checksum)
                throw new InvalidAtrException("Checksum failed");
        }
    }

    public String toHexString(String separator) {
        return HexFormat.ofDelimiter(separator).formatHex(atr.getBytes());
    }

    @Override
    public String toString() {
        return "ParsedAtr{" +
                "atr=" + toHexString(" ") +
                ", convention=" + convention +
                ", historicalByteLength=" + historicalByteLength +
                ", historicalBytes=" + HexFormat.ofDelimiter(" ").formatHex(historicalBytes) +
                '}';
    }

    public CONVENTION getConvention() {
        return convention;
    }

    public int getHistoricalByteLength() {
        return historicalByteLength;
    }

    public enum CONVENTION {
        DIRECT, INVERSE
    }
}
