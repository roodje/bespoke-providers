package com.yolt.providers.stet.bpcegroup.common.service.fetchdata;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BpceGroupFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    public BpceGroupFetchTransactionsStrategy(FetchDataRestClient restClient,
                                              DateTimeSupplier dateTimeSupplier,
                                              DefaultProperties properties,
                                              Period fetchPeriod) {
        super(restClient, dateTimeSupplier, properties, fetchPeriod);
    }

    @Override
    protected String configureUrlForInitialFetch(final String endpoint, final Instant adjustedFetchStartTime) {
        return UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("dateFrom", DateTimeFormatter.ISO_INSTANT.format(OffsetDateTime.ofInstant(Instant.ofEpochSecond(adjustedFetchStartTime.getEpochSecond()), ZoneOffset.UTC)))
                .toUriString();
    }

    @Override
    protected List<StetTransactionStatus> supportedTransactionTypes() {
        return List.of(StetTransactionStatus.BOOK);
    }
}
