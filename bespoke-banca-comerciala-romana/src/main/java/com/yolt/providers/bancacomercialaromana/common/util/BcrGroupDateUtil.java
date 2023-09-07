package com.yolt.providers.bancacomercialaromana.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BcrGroupDateUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Bucharest");

    public static String toNarrowedDateFormat(Clock clock, Instant instant, int pastDays) {
        Instant dateBoundary = Instant.now(clock).minus(Period.ofDays(pastDays));
        if (instant.isBefore(dateBoundary)) {
            return toDateFormat(dateBoundary);
        }
        return toDateFormat(instant);
    }

    private static String toDateFormat(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(DEFAULT_ZONE).format(instant);
    }

    public static ZonedDateTime getCurrentZoneDateTime(Clock clock) {
        Clock clockWithZone = clock.withZone(DEFAULT_ZONE);
        return ZonedDateTime.now(clockWithZone);
    }

    @SneakyThrows(ParseException.class)
    public static ZonedDateTime getNullableZonedDateTime(String date, String format) {
        if (StringUtils.isNotEmpty(date)) {
            Instant instant = new SimpleDateFormat(format).parse(date).toInstant();
            return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE);
        }
        return null;
    }
}
