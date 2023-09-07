package com.yolt.providers.stet.societegeneralegroup.common.mapper;

import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class SocieteGeneraleDateTimeSupplier extends DateTimeSupplier {

    private final Clock zonedClock;
    private final ZoneId zoneId;

    private static final DateTimeFormatter TRANSACTIONS_DATE_FROM_QUERY_PARAM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public SocieteGeneraleDateTimeSupplier(Clock clock) {
        super(clock);
        this.zonedClock = clock.withZone(ZoneId.of("Europe/Paris"));
        this.zoneId = ZoneId.of("Europe/Paris");
    }

    public String prepareDateFromQueryParam(Instant adjustedFetchDataStartTime) {
        return LocalDateTime.ofInstant(adjustedFetchDataStartTime, getZoneId()).format(TRANSACTIONS_DATE_FROM_QUERY_PARAM_FORMATTER);
    }

    @Override
    public ZonedDateTime convertToZonedDateTime(OffsetDateTime dateTime) {
        if (dateTime != null) {
            return ZonedDateTime.of(dateTime.toLocalDate(), LocalTime.MIN, getZoneId());
        }
        return null;
    }

    @Override
    public OffsetDateTime convertOrGetDefaultOffsetDateTime(LocalDate executionDate, OffsetDateTime defaultOffsetDateTime) {
        return executionDate != null
                ? OffsetDateTime.of(executionDate, LocalTime.of(8, 0), zoneId.getRules().getOffset(LocalDateTime.now(zonedClock))).withOffsetSameInstant(ZoneOffset.UTC)
                : defaultOffsetDateTime;
    }
}
