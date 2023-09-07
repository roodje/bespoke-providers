package com.yolt.providers.amexgroup.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneId.SHORT_IDS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmexDateTimeUtils {

    public static final ZoneId MST_TIMEZONE = ZoneId.of(SHORT_IDS.get("MST"));

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getFormattedDate(Instant input) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(input, MST_TIMEZONE));
    }

    public static ZonedDateTime getZonedDateTime(String formattedDate){
        if (StringUtils.isEmpty(formattedDate)) {
            return null;
        }
        LocalDate date = LocalDate.parse(formattedDate, DATE_TIME_FORMATTER);
        return  date.atStartOfDay(MST_TIMEZONE);
    }

    public static Instant getInstant(String formattedDate) {
        if (StringUtils.isEmpty(formattedDate)) {
            return null;
        }
        LocalDate date = LocalDate.parse(formattedDate, DATE_TIME_FORMATTER);
        return date.atStartOfDay(MST_TIMEZONE).toInstant();
    }
}