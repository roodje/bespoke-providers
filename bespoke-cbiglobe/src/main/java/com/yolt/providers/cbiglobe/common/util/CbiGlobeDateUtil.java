package com.yolt.providers.cbiglobe.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CbiGlobeDateUtil {

    private static final ZoneId EU_ROME_ZONE_ID = ZoneId.of("Europe/Rome");

    private static final String DATETIME_FORMAT = "EEE, dd MMM yyyy hh:mm:ss z";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static String formattedCurrentDateTime(Clock clock) {
        Clock clockWithZone = clock.withZone(ZoneId.of("UTC"));
        return ZonedDateTime.now(clockWithZone).format(DateTimeFormatter.ofPattern(DATETIME_FORMAT).withLocale(Locale.US));
    }

    public static String formattedValidityDateTime(Integer validityDays, Clock clock) {
        return LocalDateTime.now(clock).plusDays(validityDays).format(DateTimeFormatter.ISO_DATE);
    }

    public static Instant calculateValidityDateTime(Integer validityDays, Clock clock) {
        return Instant.now(clock).plus(validityDays, ChronoUnit.DAYS);
    }

    public static ZonedDateTime dateToZonedDateTime(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
        return localDate.atStartOfDay(EU_ROME_ZONE_ID);
    }

    public static ZonedDateTime dateTimeToZonedDateTime(String dateTime) {
        return ZonedDateTime.parse(dateTime);
    }

    public static String toDateFormat(Instant instant) {
        return DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(EU_ROME_ZONE_ID).format(instant);
    }
}
