package de.redblackmediaproduction.uscutil.libmrz.countrycodes;

import com.opencsv.bean.CsvToBeanBuilder;
import de.redblackmediaproduction.uscutil.libmrz.exception.InvalidCountryCodeException;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CountryCodeReader {
    private static final Logger logger = LogManager.getLogger(CountryCodeReader.class);
    private final Map<String, CountryInfoBean> countryInfoBeanMap = new LinkedHashMap<>();

    private void initialize() {
        List<CountryInfoBean> beans = new CsvToBeanBuilder<CountryInfoBean>(new BufferedReader(new InputStreamReader(new BOMInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("countryCodes.csv"))))))
                .withType(CountryInfoBean.class).withSeparator(';').build().parse();
        logger.trace(String.format("Initializing with %d entries", beans.size()));
        for (CountryInfoBean bean : beans) {
            logger.trace(String.format("Adding bean: %s", bean));
            countryInfoBeanMap.put(bean.getIcao930333(), bean);
        }
    }

    public CountryInfoBean getCountry(String iso31663) throws InvalidCountryCodeException {
        if (countryInfoBeanMap.isEmpty())
            initialize();
        if (!countryInfoBeanMap.containsKey(iso31663))
            throw new InvalidCountryCodeException("Unable to find country code");
        return countryInfoBeanMap.get(iso31663);
    }

    public Map<String, CountryInfoBean> getCountryInfoBeanMap() {
        return countryInfoBeanMap;
    }
}
