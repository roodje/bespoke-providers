package com.yolt.providers.stet.cmarkeagroup.common.mapper;

import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CmArkeaGroupDateTimeSupplier extends DateTimeSupplier {

    private static final DateTimeFormatter TRANSACTIONS_DATE_FROM_QUERY_PARAM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Clock zonedClock;

    public CmArkeaGroupDateTimeSupplier(Clock clock, ZoneId zoneId) {
        super(clock);
        this.zonedClock = clock.withZone(zoneId);
    }

    public String getDateForQueryParam(Instant adjustedFetchDataStartTime) {
        return LocalDateTime.ofInstant(adjustedFetchDataStartTime, getZoneId()).format(TRANSACTIONS_DATE_FROM_QUERY_PARAM_FORMATTER);
    }

    public String getCurrentDateForQueryParam() {
       return getDateForQueryParam(Instant.now(zonedClock));
    }
}
