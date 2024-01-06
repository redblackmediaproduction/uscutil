package de.redblackmediaproduction.uscutil.libmrz.mrz;

import de.redblackmediaproduction.uscutil.libmrz.countrycodes.CountryCodeReader;
import de.redblackmediaproduction.uscutil.libmrz.countrycodes.CountryInfoBean;
import de.redblackmediaproduction.uscutil.libmrz.exception.InvalidCountryCodeException;
import de.redblackmediaproduction.uscutil.libmrz.exception.InvalidMrzException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Represents an ICAO 9303 compliant ID card
 * <p>See <a href="https://en.wikipedia.org/wiki/Machine-readable_passport">Wikipedia</a></p>
 */
public class ParsedMrz {
    private static final Logger logger = LogManager.getLogger(ParsedMrz.class);
    protected FORMATS mrzFormat = FORMATS.UNKNOWN;
    protected String line1;
    protected String line2;
    protected String line3;
    protected TYPES primaryType = TYPES.UNKNOWN;
    protected String secondaryType = "";
    protected CountryInfoBean issuingCountry = null;
    protected String surname = "";
    protected String[] givenNames = {};
    protected String documentNumber;
    protected CountryInfoBean citizenship = null;
    protected Date dateOfBirth = null;
    protected GENDERS gender = GENDERS.UNKNOWN;
    protected Date dateOfExpiry = null;
    /**
     * Personal number / Tax ID (e.g. former-Yugoslavian OIB) / whatever (at issuer state discretion)
     */
    protected String optionalData = "";
    /**
     * Employer field for Crew Membership Certificates
     * See ICAO 9303-5, section C.2
     */
    protected String cmcEmployer = "";
    SimpleDateFormat idCardFormatter = new SimpleDateFormat("yyMMdd");
    CountryCodeReader countryCodeReader = new CountryCodeReader();

    public void setFormat(FORMATS mrzFormat) {
        this.mrzFormat = mrzFormat;
    }

    public void setLines(String line1, String line2) throws InvalidMrzException, FileNotFoundException {
        if (this.mrzFormat == FORMATS.UNKNOWN)
            throw new InvalidMrzException("Tried to set line content without prior setting of document format");

        this.line1 = line1;
        this.line2 = line2;
        this.line3 = "";
        parseLines();
    }

    public void setLines(String line1, String line2, String line3) throws InvalidMrzException, FileNotFoundException {
        if (this.mrzFormat == FORMATS.UNKNOWN)
            throw new InvalidMrzException("Tried to set line content without prior setting of document format");

        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        parseLines();
    }

    private void parseLines() throws InvalidMrzException, FileNotFoundException {
        switch (this.mrzFormat) {
            case TYPE1:
                parseType1();
                break;
            case TYPE2:
                parseType2();
                break;
            case TYPE3:
                parseType3();
                break;
            default:
                throw new InvalidMrzException("Invalid document format");
        }
    }

    /**
     * Read primary and secondary type
     * Across all formats, line 1 is primary type, secondary type and issuing country
     */
    private void readBaseInformation() throws InvalidMrzException, FileNotFoundException {
        String primaryType = line1.substring(0, 1);
        String secondaryType = line1.substring(1, 2);
        logger.trace(String.format("primaryType raw: '%s'", primaryType));
        logger.trace(String.format("secondaryType raw: '%s'", secondaryType));
        switch (primaryType) {
            case "P":
                this.primaryType = TYPES.PASSPORT;
                break;
            case "I":
                this.primaryType = TYPES.ID_CARD;
                break;
            case "A": //Aircraft Crew Member Certificates
                if (!secondaryType.equals("C"))
                    throw new InvalidMrzException("primaryType is A, but secondaryType not C");
                this.primaryType = TYPES.CREW_CARD;
                break;
            case "V":
                this.primaryType = TYPES.VISA;
                break;
            default:
                throw new InvalidMrzException("Invalid primary type");
        }
        this.secondaryType = secondaryType.replace("<", "");
        String issuingCountry = line1.substring(2, 5);
        logger.trace(String.format("issuingCountry raw: '%s'", issuingCountry));
        this.issuingCountry = countryCodeReader.getCountry(issuingCountry.replace("<", ""));
    }

    private void readAllNames(String line, int beginIndex, int endIndex) throws InvalidMrzException {
        String allNames = line.substring(beginIndex, endIndex);
        logger.trace(String.format("allNames raw: '%s'", allNames));
        String[] allNamesList = allNames.replace("<", " ").replace("  ", " ").trim().split(" ");
        if (allNamesList.length == 0)
            throw new InvalidMrzException("No name records found");
        this.surname = allNamesList[0];
        logger.trace(String.format("%d names in record", allNamesList.length));
        if (allNamesList.length > 1) {
            this.givenNames = Arrays.copyOfRange(allNamesList, 1, allNamesList.length);
        }
    }

    private void readDateOfBirth(String line, int beginIndex) throws InvalidMrzException {
        String dateOfBirth = line.substring(beginIndex, beginIndex + 6);
        logger.trace(String.format("dob raw: '%s'", dateOfBirth));
        int dateOfBirthChecksumExpected = Integer.parseInt(line.substring(beginIndex + 6, beginIndex + 7));
        int dateOfBirthChecksumActual = calculateChecksum(dateOfBirth);
        if (dateOfBirthChecksumActual != dateOfBirthChecksumExpected)
            throw new InvalidMrzException("Failed to match dateOfBirth checksum");
        try {
            this.dateOfBirth = idCardFormatter.parse(dateOfBirth);
        } catch (Exception e) {
            throw new InvalidMrzException("Failed to parse dateOfBirth");
        }
    }

    private void readDateOfExpiry(String line, int beginIndex) throws InvalidMrzException {
        String dateOfExpiry = line.substring(beginIndex, beginIndex + 6);
        logger.trace(String.format("doe raw: '%s'", dateOfExpiry));
        int dateOfExpiryChecksumExpected = Integer.parseInt(line.substring(beginIndex + 6, beginIndex + 7));
        int dateOfExpiryChecksumActual = calculateChecksum(dateOfExpiry);
        if (dateOfExpiryChecksumActual != dateOfExpiryChecksumExpected)
            throw new InvalidMrzException("Failed to match dateOfExpiry checksum");
        try {
            this.dateOfExpiry = idCardFormatter.parse(dateOfExpiry);
        } catch (Exception e) {
            throw new InvalidMrzException("Failed to parse dateOfExpiry");
        }
    }

    private void readGender(String line, int beginIndex) throws InvalidMrzException {
        String gender = line.substring(beginIndex, beginIndex + 1);
        logger.trace(String.format("Raw gender: '%s'", gender));
        switch (gender.replace("<", "")) {
            case "":
                this.gender = GENDERS.UNKNOWN;
                break;
            case "X":
                this.gender = GENDERS.THIRD;
                break;
            case "M":
                this.gender = GENDERS.MALE;
                break;
            case "F":
                this.gender = GENDERS.FEMALE;
                break;
            default:
                throw new InvalidMrzException("Invalid gender field");
        }
    }

    private void readCitizenship(String line, int beginIndex) throws FileNotFoundException, InvalidCountryCodeException {
        String citizenship = line.substring(beginIndex, beginIndex + 3);
        logger.trace(String.format("Citizenship raw: %s", citizenship));
        this.citizenship = countryCodeReader.getCountry(citizenship.replace("<", ""));
    }

    private void readCmcEmployer(String line, int beginIndex) {
        String cmcEmployer = line.substring(beginIndex, beginIndex + 3);
        logger.trace(String.format("cmcEmployer raw: %s", cmcEmployer));
        this.cmcEmployer = cmcEmployer.replace("<", "");
    }

    private void readDocumentNumber(String line, int beginIndex, boolean checkLongMode) throws InvalidMrzException {
        String documentNumber = line.substring(beginIndex, beginIndex + 9);
        logger.trace(String.format("documentNumber raw: '%s'", documentNumber));
        String documentNumberChecksumExpected = line.substring(beginIndex + 9, beginIndex + 10);
        logger.trace(String.format("documentNumberChecksum raw: '%s'", documentNumberChecksumExpected));

        //Long mode according to ICAO 9303-5 subsection 4.2.4
        if (checkLongMode && "<".equals(documentNumberChecksumExpected)) {
            String documentNumberRemainder = line.substring(beginIndex + 10);
            logger.trace(String.format("documentNumberRemainder raw: '%s'", documentNumber));
            documentNumberRemainder = documentNumberRemainder.replace("<", "");
            documentNumberChecksumExpected = documentNumberRemainder.substring(documentNumberRemainder.length() - 1);
            documentNumber += documentNumberRemainder.substring(0, documentNumberRemainder.length() - 1);
        }
        this.documentNumber = documentNumber.replace("<", "");
        int documentNumberChecksumActual = calculateChecksum(documentNumber);
        if (documentNumberChecksumActual != Integer.parseInt(documentNumberChecksumExpected))
            throw new InvalidMrzException("Failed to match document number checksum");
    }

    private void readOptionalData(String line, int beginIndex, int endIndex, boolean hasChecksum) throws InvalidMrzException {
        String optionalData = line.substring(beginIndex, endIndex);
        logger.trace(String.format("optionalData raw: '%s'", optionalData));
        if (hasChecksum) {
            String optionalDataChecksum = line.substring(endIndex, endIndex + 1);
            logger.trace(String.format("optionalDataChecksum raw: '%s'", optionalDataChecksum));
            if (!optionalDataChecksum.replace("<", "").isEmpty()) {
                int optionalDataChecksumExpected = Integer.parseInt(optionalDataChecksum);
                int optionalDataChecksumActual = calculateChecksum(optionalData);
                if (optionalDataChecksumActual != optionalDataChecksumExpected)
                    throw new InvalidMrzException("Failed to match optionalData checksum");
            }
        }
        this.optionalData = optionalData.replace("<", "");
    }

    /**
     * Parse a Type 1 MRZ (ICAO 9303-5), 3x30
     * <p>See <a href="https://www.icao.int/publications/Documents/9303_p5_cons_en.pdf">spec</a></p>
     *
     * @throws InvalidMrzException
     */
    private void parseType1() throws InvalidMrzException, FileNotFoundException {
        //Line 1
        readBaseInformation();
        switch (primaryType) {
            case CREW_CARD:
                readDocumentNumber(line1, 5, false);
                readCmcEmployer(line1, 15);
                //todo: German CMCs include the name of the issuing LBA office afterwards. How to represent this? Need more CMCs from other countries.
            case ID_CARD:
            case PASSPORT:
            default:
                readDocumentNumber(line1, 5, true);
        }

        //Line 2
        readDateOfBirth(line2, 0);
        readGender(line2, 7);
        readDateOfExpiry(line2, 8);
        readCitizenship(line2, 15);
        readOptionalData(line2, 18, 29, false);

        String overallChecksum = line2.substring(29, 30);
        logger.trace(String.format("overallChecksum raw: '%s'", overallChecksum));
        String overall = line1.substring(5, 30) + line2.substring(0, 7) + line2.substring(8, 15) + line2.substring(18, 29);
        int overallChecksumExpected = Integer.parseInt(overallChecksum);
        int overallChecksumActual = calculateChecksum(overall);
        if (overallChecksumActual != overallChecksumExpected)
            throw new InvalidMrzException("Failed to match overall checksum");

        //Line 3
        this.readAllNames(line3, 0, 30);
    }

    /**
     * Parse a Type 2 MRZ (ICAO 9303-6), 2x36
     * <p>See <a href="https://www.icao.int/publications/Documents/9303_p6_cons_en.pdf">spec</a></p>
     *
     * @throws InvalidMrzException
     */
    private void parseType2() throws InvalidMrzException, FileNotFoundException {
        //Line 1
        readBaseInformation();
        this.readAllNames(line1, 5, 36);

        //Line 2
        readDocumentNumber(line2, 0, false);
        readCitizenship(line2, 10);
        readDateOfBirth(line2, 13);
        readGender(line2, 20);
        readDateOfExpiry(line2, 21);
        readOptionalData(line2, 28, 35, false);

        switch (this.primaryType) {
            case VISA:
                break;
            case PASSPORT:
            case ID_CARD:
                String overallChecksum = line2.substring(35, 36);
                logger.trace(String.format("overallChecksum raw: '%s'", overallChecksum));
                String overall = line2.substring(0, 10) + line2.substring(13, 20) + line2.substring(21, 35);
                int overallChecksumExpected = Integer.parseInt(overallChecksum);
                int overallChecksumActual = calculateChecksum(overall);
                if (overallChecksumActual != overallChecksumExpected)
                    throw new InvalidMrzException("Failed to match overall checksum");
                break;
        }
    }

    /**
     * Parse a Type 3 MRZ (ICAO 9303-4), 2x44
     * <p>See <a href="https://www.icao.int/publications/Documents/9303_p4_cons_en.pdf">spec</a></p>
     *
     * @throws InvalidMrzException
     */
    private void parseType3() throws InvalidMrzException, FileNotFoundException {
        //Line 1
        readBaseInformation();
        readAllNames(line1, 5, 44);

        //Line 2
        readDocumentNumber(line2, 0, false);
        readCitizenship(line2, 10);
        readDateOfBirth(line2, 13);
        readGender(line2, 20);
        readDateOfExpiry(line2, 21);
        switch (this.primaryType) {
            case VISA:
                readOptionalData(line2, 28, 43, false);
                break;
            case PASSPORT:
            case ID_CARD:
            default:
                readOptionalData(line2, 28, 42, true);
                break;

        }
        switch (this.primaryType) {
            case VISA:
                break;
            case PASSPORT:
            case ID_CARD:
            default:
                String overallChecksum = line2.substring(43, 44);
                logger.trace(String.format("overallChecksum raw: '%s'", overallChecksum));
                String overall = line2.substring(0, 10) + line2.substring(13, 20) + line2.substring(21, 43);
                int overallChecksumExpected = Integer.parseInt(overallChecksum);
                int overallChecksumActual = calculateChecksum(overall);
                if (overallChecksumActual != overallChecksumExpected)
                    throw new InvalidMrzException("Failed to match overall checksum");
                break;
        }

    }

    /**
     * Calculate a checksum
     *
     * <p>See <a href="https://www.icao.int/publications/Documents/9303_p3_cons_en.pdf">spec</a>, section 4.9</p>
     *
     * @param input
     * @return
     */
    public int calculateChecksum(String input) {
        int[] weights = {7, 3, 1};
        int sum = 0;
        for (int i = 0; i < input.length(); i++) {
            int charCode = (int) input.charAt(i);
            logger.trace(String.format("At %d, weight index %d, charCode %d, char '%c'", i, i % 3, charCode, charCode));
            if (charCode >= 48 && charCode <= 57) { // Numbers
                sum += weights[i % 3] * (charCode - 48);
            } else if (charCode >= 65 && charCode <= 90) { //A-Z
                sum += weights[i % 3] * (charCode - 55);
            } else if (charCode == 60) { //Filler (<)
                //Zero. No effect on sum
            }
        }
        int ret = sum % 10;
        logger.trace(String.format("Sum: %d, checksum: %d", sum, ret));
        return ret;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }

    @Override
    public String toString() {
        return "ParsedMrz{" +
                "idCardFormatter=" + idCardFormatter +
                ", mrzFormat=" + mrzFormat +
                ", line1='" + line1 + '\'' +
                ", line2='" + line2 + '\'' +
                ", line3='" + line3 + '\'' +
                ", primaryType=" + primaryType +
                ", secondaryType='" + secondaryType + '\'' +
                ", issuingCountry='" + issuingCountry + '\'' +
                ", surname='" + surname + '\'' +
                ", givenNames=" + Arrays.toString(givenNames) +
                ", documentNumber='" + documentNumber + '\'' +
                ", citizenship='" + citizenship + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", dateOfExpiry=" + dateOfExpiry +
                ", optionalData='" + optionalData + '\'' +
                ", cmcEmployer='" + cmcEmployer + '\'' +
                '}';
    }

    public enum FORMATS {
        UNKNOWN,
        TYPE1, //3x30
        TYPE2, //2x36
        TYPE3, //2x44
    }

    public enum TYPES {
        UNKNOWN,
        PASSPORT, //P
        ID_CARD, //I
        CREW_CARD, //A
        //C => unknown
        VISA, //V
    }

    public enum GENDERS {
        UNKNOWN, //<
        MALE, //M
        FEMALE, //F
        THIRD, //X
    }

}
