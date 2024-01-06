module de.redblackmediaproduction.uscutil.libmrz {
    exports de.redblackmediaproduction.uscutil.libmrz;
    exports de.redblackmediaproduction.uscutil.libmrz.countrycodes;
    exports de.redblackmediaproduction.uscutil.libmrz.mrz;
    exports de.redblackmediaproduction.uscutil.libmrz.exception;
    opens de.redblackmediaproduction.uscutil.libmrz.countrycodes;

    requires org.apache.logging.log4j;
    requires com.opencsv;
    //for opencsv, see https://stackoverflow.com/questions/57824169/opencsv-runtime-error-noclassdeffounderror-java-sql-date#comment102091948_57824169
    requires java.sql;
    //for opencsv to read Excel-exported CSVs, see https://stackoverflow.com/a/56222034
    requires org.apache.commons.io;
}
