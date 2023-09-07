package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time;

import lombok.AllArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultDateTimeMapper implements Function<String, ZonedDateTime> {
    private final ZoneId zoneId;

    @Override
    public ZonedDateTime apply(String date) {
        return ZonedDateTime.parse(date).withZoneSameInstant(zoneId);
    }
}
