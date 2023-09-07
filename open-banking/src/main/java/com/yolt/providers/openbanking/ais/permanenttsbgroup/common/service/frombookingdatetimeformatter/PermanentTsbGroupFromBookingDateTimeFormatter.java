package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service.frombookingdatetimeformatter;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@NoArgsConstructor
public class PermanentTsbGroupFromBookingDateTimeFormatter implements Function<Instant, String> {

    @Override
    public String apply(Instant transactionsFetchStartTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
    }
}
