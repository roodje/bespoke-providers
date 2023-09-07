package com.yolt.providers.brdgroup.common.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RequiredArgsConstructor
public class BrdGroupDateConverter {

    private final Clock defaultClock;
    private final ZoneId zoneId;

    public Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public ZonedDateTime getDefaultZonedDateTime() {
        return ZonedDateTime.now(defaultClock);
    }

    public ZonedDateTime getNullableZonedDateTime(String date) {
        if (StringUtils.isNotEmpty(date)) {
            return LocalDate.parse(date).atStartOfDay(zoneId);
        }
        return null;
    }

    public ZonedDateTime determineTransactionDateTime(String bookingDateTime, String valueDateTime) {
        if (StringUtils.isNotEmpty(bookingDateTime)) {
            return LocalDate.parse(bookingDateTime).atStartOfDay(zoneId);
        }
        if (StringUtils.isNotEmpty(valueDateTime)) {
            return LocalDate.parse(valueDateTime).atStartOfDay(zoneId);
        }
        return null;
    }

    public String toDateFormat(Instant instant) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instant);
    }
}
