package com.yolt.providers.stet.generic.mapper;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Getter
public class DateTimeSupplier {

    private final Clock defaultClock;
    private final Clock zonedClock;
    private final ZoneId zoneId;

    public DateTimeSupplier(Clock clock) {
        this(clock, ZoneId.of("Europe/Paris"));
    }

    public DateTimeSupplier(Clock clock, ZoneId zoneId) {
        this.defaultClock = clock;
        this.zonedClock = clock.withZone(zoneId);
        this.zoneId = zoneId;
    }

    public ZonedDateTime getDefaultZonedDateTime() {
        return ZonedDateTime.now(defaultClock);
    }

    public Instant getDefaultInstant() {
        return Instant.now(defaultClock);
    }

    public OffsetDateTime getDefaultOffsetDateTime() {
        return OffsetDateTime.now(defaultClock);
    }

    public LocalDate getZonedLocalDate() {
        return LocalDate.now(zonedClock);
    }

    public OffsetDateTime parseToOffsetDateTime(String date, DateTimeFormatter formatter) {
        // handling date value "" , because it was noticed in bank's response.
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return LocalDate.parse(date, formatter)
                .atStartOfDay(zoneId)
                .toOffsetDateTime();
    }

    public OffsetDateTime convertOrGetDefaultOffsetDateTime(LocalDate executionDate, OffsetDateTime defaultOffsetDateTime) {
        return executionDate != null
                ? OffsetDateTime.of(executionDate, LocalTime.of(8, 0), zoneId.getRules().getOffset(LocalDateTime.now(zonedClock)))
                : defaultOffsetDateTime;
    }

    public LocalDate convertToLocalDate(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneId).toLocalDate();
    }

    public ZonedDateTime convertToZonedDateTime(OffsetDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(date -> dateTime.atZoneSameInstant(zoneId))
                .orElse(null);
    }
}