package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper;

import lombok.RequiredArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupDateMapper implements RaiffeisenAtGroupDateMapper {

    private final ZoneId zoneId;

    private final Clock clock;

    public Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public String toDateFormat(Instant instant) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instant);
    }

    @Override
    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now(clock);
    }

    @Override
    public ZonedDateTime getZonedDateTime(LocalDate date) {
        return date.atStartOfDay(zoneId);
    }
}
