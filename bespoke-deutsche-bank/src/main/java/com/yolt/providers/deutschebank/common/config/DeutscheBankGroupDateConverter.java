package com.yolt.providers.deutschebank.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DeutscheBankGroupDateConverter {

    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Clock defaultClock;
    private final Clock zonedClock;
    private final ZoneId zoneId;

    public DeutscheBankGroupDateConverter(Clock clock, ZoneId zoneId) {
        this.defaultClock = clock;
        this.zonedClock = clock.withZone(zoneId);
        this.zoneId = zoneId;
    }

    public Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public ZonedDateTime getZonedDateTimeWhenDateTimeIsMissing(String value) {
        Matcher matcher = DATE_TIME_PATTERN.matcher(value);
        if (StringUtils.isNotEmpty(value) && matcher.find()) {
            return LocalDateTime.parse(matcher.group(), DATE_TIME_FORMATTER).atZone(zoneId);
        }
        return ZonedDateTime.now(zonedClock);
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

    public String toNarrowedDateFormat(Instant instant, int periodDays) {
        Instant ninetyDaysBoundary = Instant.now(defaultClock).minus(Period.ofDays(periodDays));
        if (instant.isBefore(ninetyDaysBoundary)) {
            return toDateFormat(ninetyDaysBoundary);
        }
        return toDateFormat(instant);
    }

    private String toDateFormat(Instant instant) {
        return DateTimeFormatter.ISO_LOCAL_DATE.withZone(zoneId).format(instant);
    }
}
