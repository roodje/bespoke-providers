package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;

import java.time.*;

public class LclDateTimeSupplier extends DateTimeSupplier {

    public LclDateTimeSupplier(final Clock clock) {
        super(clock);
    }

    @Override
    public ZonedDateTime convertToZonedDateTime(final OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay(ZoneId.of("Europe/Paris")).withZoneSameInstant(ZoneId.of("Z"));
    }

    @Override
    public ZonedDateTime getDefaultZonedDateTime() {
        Clock clock = Clock.systemUTC();
        return ZonedDateTime.now(clock);
    }
}
