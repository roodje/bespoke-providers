package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.frombookingdatetimeformatter;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@NoArgsConstructor
public class Iso8601FromBookingDateTimeFormatter implements Function<Instant, String> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public String apply(Instant transactionsFetchStartTime) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
    }
}
