package com.yolt.providers.stet.bnpparibasgroup.common.mapper;

import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BnpParibasGroupDataTimeSupplier extends DateTimeSupplier {

    public BnpParibasGroupDataTimeSupplier(Clock clock, ZoneId zoneId) {
        super(clock, zoneId);
    }

    @Override
    public ZonedDateTime convertToZonedDateTime(final OffsetDateTime dateTime) {
        return dateTime == null ? null :
                dateTime.toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris")).withZoneSameInstant(ZoneId.of("Z"));
    }
}
