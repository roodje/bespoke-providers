package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import lombok.RequiredArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RequiredArgsConstructor
public class DefaultCheBancaGroupDateMapper implements CheBancaGroupDateMapper {

    private final ZoneId zoneId;

    private final Clock clock;

    public Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public String toDateFormat(final Instant instant) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instant);
    }

    @Override
    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now(clock);
    }

    @Override
    public ZonedDateTime getZonedDateTime(final LocalDate date) {
        return date.atStartOfDay(zoneId);
    }
}
