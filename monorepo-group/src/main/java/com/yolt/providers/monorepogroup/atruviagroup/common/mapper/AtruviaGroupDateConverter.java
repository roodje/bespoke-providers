package com.yolt.providers.monorepogroup.atruviagroup.common.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class AtruviaGroupDateConverter {

    private final Clock defaultClock;
    private final ZoneId zoneId;

    public ZonedDateTime getDefaultZonedDateTime() {
        return ZonedDateTime.now(defaultClock);
    }

    public ZonedDateTime getNullableZonedDateTime(String date) {
        if (StringUtils.isNotEmpty(date)) {
            return LocalDate.parse(date).atStartOfDay(zoneId);
        }
        return null;
    }

    public String toDateFormat(Instant instant) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instant);
    }
}
