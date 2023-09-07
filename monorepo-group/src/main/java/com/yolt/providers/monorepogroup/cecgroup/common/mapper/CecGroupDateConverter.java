package com.yolt.providers.monorepogroup.cecgroup.common.mapper;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RequiredArgsConstructor
public class CecGroupDateConverter {

    private final ZoneId zoneId;

    public Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public Date toDate(Long expirationTimestamp) {
        return Date.from(Instant.ofEpochMilli(expirationTimestamp));
    }

    public Long toTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    public String toIsoDate(Instant instant) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instant);
    }
}
