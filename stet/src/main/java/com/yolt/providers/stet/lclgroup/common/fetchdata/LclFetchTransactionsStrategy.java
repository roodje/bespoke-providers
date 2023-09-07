package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LclFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public LclFetchTransactionsStrategy(final FetchDataRestClient restClient,
                                        final DateTimeSupplier dateTimeSupplier,
                                        final DefaultProperties properties,
                                        final Period period) {
        super(restClient, dateTimeSupplier, properties, period);
    }

    @Override
    protected String configureUrlForInitialFetch(final String endpoint, final Instant adjustedFetchStartTime) {
        String formattedStartTime = DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(adjustedFetchStartTime, ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
        return UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("dateFrom", formattedStartTime)
                .toUriString();
    }
}
