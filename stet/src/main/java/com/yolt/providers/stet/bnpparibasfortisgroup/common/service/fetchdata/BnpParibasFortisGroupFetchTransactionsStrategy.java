package com.yolt.providers.stet.bnpparibasfortisgroup.common.service.fetchdata;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.PaginationDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.Period;
import java.util.*;

public class BnpParibasFortisGroupFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    private static final String ENDPOINT_VERSION_PREFIX = "/psd2/v2";

    public BnpParibasFortisGroupFetchTransactionsStrategy(FetchDataRestClient restClient,
                                                          DateTimeSupplier dateTimeSupplier,
                                                          DefaultProperties properties,
                                                          Period fetchPeriod) {
        super(restClient, dateTimeSupplier, properties, fetchPeriod);
    }

    @Override
    public List<StetTransactionDTO> fetchTransactions(HttpClient httpClient,
                                                      String endpoint,
                                                      DataRequest dataRequest,
                                                      Instant fetchStartTime) throws TokenInvalidException {
        List<StetTransactionDTO> allTransactions = new ArrayList<>();

        Instant adjustedFetchStartTime = adjustFetchStartTime(dataRequest, fetchStartTime);
        String url = configureUrlForInitialFetch(endpoint, adjustedFetchStartTime);

        for (int pageCounter = 1; StringUtils.hasText(url) && (pageCounter <= properties.getPaginationLimit()); pageCounter++) {
            Optional<StetTransactionsResponseDTO> optionalHalTransactions = Optional.ofNullable(
                    restClient.getTransactions(httpClient, url, dataRequest));

            optionalHalTransactions.ifPresent(halTransactions -> {
                if (Objects.nonNull(halTransactions.getTransactions())) {
                    for(StetTransactionDTO transaction : new ArrayList<>(halTransactions.getTransactions())) {
                        if (!shouldIgnoreTransaction(transaction)) {
                            allTransactions.add(transaction);
                        }
                    }
                }
            });
            url = optionalHalTransactions
                    .map(StetTransactionsResponseDTO::getLinks)
                    .map(PaginationDTO::getNext)
                    .orElse("");

            if (!url.isEmpty() && !url.contains(ENDPOINT_VERSION_PREFIX)) {
                url = ENDPOINT_VERSION_PREFIX + url;
            }
        }
        return allTransactions;
    }

    protected String configureUrlForInitialFetch(String endpoint, Instant adjustedFestStartTime) {
        return UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("dateFrom", dateTimeSupplier.convertToLocalDate(adjustedFestStartTime))
                .queryParam("dateTo", dateTimeSupplier.getZonedLocalDate())
                .toUriString();
    }


    protected Instant adjustFetchStartTime(DataRequest dataRequest, Instant fetchStartTime) { //NOSONAR It is provided to customize logic for fetch start time adjustment
        Instant fetchEndTime = dateTimeSupplier.getDefaultInstant().minus(fetchPeriod);
        return fetchStartTime.isBefore(fetchEndTime) ? fetchEndTime : fetchStartTime;
    }
}
