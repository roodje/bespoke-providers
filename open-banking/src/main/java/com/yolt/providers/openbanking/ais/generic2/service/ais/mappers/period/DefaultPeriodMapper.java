package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period;

import org.springframework.util.StringUtils;

import java.time.Period;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public class DefaultPeriodMapper implements Function<String, Period> {

    private static final String EVERY_DAY = "evryday";
    private static final String EVERY_WORKING_DAY = "evryworkgday";
    private static final String WEEKLY_INTERVAL = "intrvlwkday";
    private static final String MONTHLY_INTERVAL = "intrvlmnthday";
    private static final String MONTHLY = "wkinmnthday";
    private static final String QUARTERLY = "qtrday";

    @Override
    public Period apply(String frequency) {
        if (StringUtils.isEmpty(frequency)) {
            return Period.ZERO;
        }
        String lowerCaseFrequency = frequency.toLowerCase(Locale.ENGLISH);
        if (notCompliantWithOBStandard(lowerCaseFrequency)) {
            return Period.ZERO;
        }
        if (lowerCaseFrequency.startsWith(EVERY_DAY) || lowerCaseFrequency.startsWith(EVERY_WORKING_DAY)) {
            return Period.ofDays(1);
        } else if (lowerCaseFrequency.startsWith(WEEKLY_INTERVAL)) {
            return Period.ofWeeks(1);
        } else if (lowerCaseFrequency.startsWith(MONTHLY) || lowerCaseFrequency.startsWith(MONTHLY_INTERVAL)) {
            return Period.ofMonths(1);
        } else if (lowerCaseFrequency.startsWith(QUARTERLY)) {
            return Period.ofMonths(3);
        }
        return null;
    }

    private boolean notCompliantWithOBStandard(final String frequency) {
        String regex = "^(notknown)$|^(evryday)$|^(evryworkgday)$|^(intrvlday:((0[2-9])|([1-2][0-9])|3[0-1]))$|^(intrvlwkday:0[1-9]:0[1-7])$|^(wkinmnthday:0[1-5]:0[1-7])$|^(intrvlmnthday:(0[1-6]|12|24):(-0[1-5]|0[1-9]|[12][0-9]|3[01]))$|^(QtrDay:(english|scottish|received))$";
        return !Pattern.matches(regex, frequency) || frequency.length() > 35;
    }

}
