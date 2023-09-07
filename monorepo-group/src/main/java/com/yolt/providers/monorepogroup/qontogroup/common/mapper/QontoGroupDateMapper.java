package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import lombok.RequiredArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class QontoGroupDateMapper {

    private final ZoneId zoneId;
    private final Clock clock;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public String mapHttpRequestDateFormat(Instant dateFrom) {
        return LocalDateTime.ofInstant(dateFrom, ZoneId.of("UTC")).format(DATE_TIME_FORMATTER);
    }

    public ZonedDateTime getZonedDateTimeWithDefaultClockZoneId() {
        return ZonedDateTime.now(clock);
    }

    public ZonedDateTime getZoneDateTime() {
        return ZonedDateTime.now(clock).withZoneSameInstant(zoneId);
    }

    public ZonedDateTime toZonedDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toZonedDateTime().withZoneSameInstant(zoneId);
    }
}
