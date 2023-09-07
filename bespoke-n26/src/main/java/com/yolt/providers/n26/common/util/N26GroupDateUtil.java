package com.yolt.providers.n26.common.util;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class N26GroupDateUtil {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Berlin");

    public static String toDateFormat(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(DEFAULT_ZONE).format(instant);
    }

    public static ZonedDateTime getCurrentZoneDateTime(Clock clock) {
        return clock.instant().atZone(DEFAULT_ZONE);
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
