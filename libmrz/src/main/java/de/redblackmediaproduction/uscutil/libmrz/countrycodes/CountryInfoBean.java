package de.redblackmediaproduction.uscutil.libmrz.countrycodes;

import com.opencsv.bean.CsvBindByName;

public class CountryInfoBean {
    @CsvBindByName(column = "nameEnglish")
    private String nameEnglish;
    @CsvBindByName
    private String nameFrench;
    @CsvBindByName
    private String iso3166a2;
    @CsvBindByName
    private String iso3166a3;
    @CsvBindByName(column = "icao9303-3-2")
    private String icao930332;
    @CsvBindByName(column = "icao9303-3-3")
    private String icao930333;
    @CsvBindByName(column = "icao9303-3-section")
    private String icao93033section;
    @CsvBindByName
    private boolean isOfficial;

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getNameFrench() {
        return nameFrench;
    }

    public void setNameFrench(String nameFrench) {
        this.nameFrench = nameFrench;
    }

    public String getIso3166a2() {
        return iso3166a2;
    }

    public void setIso3166a2(String iso3166a2) {
        this.iso3166a2 = iso3166a2;
    }

    public String getIso3166a3() {
        return iso3166a3;
    }

    public void setIso3166a3(String iso3166a3) {
        this.iso3166a3 = iso3166a3;
    }

    public String getIcao930332() {
        return icao930332;
    }

    public void setIcao930332(String icao930332) {
        this.icao930332 = icao930332;
    }

    public String getIcao930333() {
        return icao930333;
    }

    public void setIcao930333(String icao930333) {
        this.icao930333 = icao930333;
    }

    public String getIcao93033section() {
        return icao93033section;
    }

    public void setIcao93033section(String icao93033section) {
        this.icao93033section = icao93033section;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        isOfficial = official;
    }

    @Override
    public String toString() {
        return "CountryInfoBean{" +
                "nameEnglish='" + nameEnglish + '\'' +
                ", nameFrench='" + nameFrench + '\'' +
                ", iso3166a2='" + iso3166a2 + '\'' +
                ", iso3166a3='" + iso3166a3 + '\'' +
                ", icao930332='" + icao930332 + '\'' +
                ", icao930333='" + icao930333 + '\'' +
                ", icao93033section='" + icao93033section + '\'' +
                ", isOfficial=" + isOfficial +
                '}';
    }
}
