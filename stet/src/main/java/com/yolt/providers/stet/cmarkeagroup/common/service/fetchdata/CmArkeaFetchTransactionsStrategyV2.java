package com.yolt.providers.stet.cmarkeagroup.common.service.fetchdata;

import com.yolt.providers.stet.cmarkeagroup.common.mapper.CmArkeaGroupDateTimeSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.Period;
import java.util.Optional;

public class CmArkeaFetchTransactionsStrategyV2 extends DefaultFetchTransactionsStrategy {

    private final CmArkeaGroupDateTimeSupplier dateTimeSupplier;

    public CmArkeaFetchTransactionsStrategyV2(FetchDataRestClient restClient,
                                              CmArkeaGroupDateTimeSupplier dateTimeSupplier,
                                              DefaultProperties properties,
                                              Period fetchPeriod) {
        super(restClient, dateTimeSupplier, properties, fetchPeriod);
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    protected String getNexPageUrl(Optional<StetTransactionsResponseDTO> optionalHalTransactions) {
        return optionalHalTransactions
                .map(StetTransactionsResponseDTO::getLinks)
                .map(links -> StringUtils.isNotEmpty(links.getSelf()) && StringUtils.isNotEmpty(links.getNext())
                        && links.getSelf().equals(links.getNext()) ? "" : links.getNext())
                .orElse("");
    }

    @Override
    protected String configureUrlForInitialFetch(String endpoint, Instant adjustedFestStartTime) {
        return UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("dateFrom", dateTimeSupplier.getDateForQueryParam(adjustedFestStartTime))
                .queryParam("dateTo", dateTimeSupplier.getCurrentDateForQueryParam())
                .toUriString()
                .replace(":", "%3A");
    }

}
