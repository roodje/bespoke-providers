package com.yolt.providers.stet.generic.service.fetchdata.transaction;

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
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.Period;
import java.util.*;

@RequiredArgsConstructor
public class DefaultFetchTransactionsStrategy implements FetchTransactionsStrategy {

    protected final FetchDataRestClient restClient;
    protected final DateTimeSupplier dateTimeSupplier;
    protected final DefaultProperties properties;
    protected final Period fetchPeriod;

    public DefaultFetchTransactionsStrategy(FetchDataRestClient restClient,
                                            DateTimeSupplier dateTimeSupplier,
                                            DefaultProperties properties) {
        this.restClient = restClient;
        this.dateTimeSupplier = dateTimeSupplier;
        this.properties = properties;
        this.fetchPeriod = Period.ofDays(365);
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
                    for (StetTransactionDTO transaction : new ArrayList<>(halTransactions.getTransactions())) {
                        if (!shouldIgnoreTransaction(transaction)) {
                            allTransactions.add(transaction);
                        }
                    }
                }
            });
            url = getNexPageUrl(optionalHalTransactions);

        }
        return allTransactions;
    }

    protected String getNexPageUrl(Optional<StetTransactionsResponseDTO> optionalHalTransactions) {
        return optionalHalTransactions
                .map(StetTransactionsResponseDTO::getLinks)
                .map(PaginationDTO::getNext)
                .orElse("");
    }

    protected String configureUrlForInitialFetch(String endpoint, Instant adjustedFestStartTime) {
        return UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("dateFrom", dateTimeSupplier.convertToLocalDate(adjustedFestStartTime))
                .queryParam("dateTo", dateTimeSupplier.getZonedLocalDate())
                .toUriString();
    }

    protected boolean shouldIgnoreTransaction(StetTransactionDTO transaction) {
        return !supportedTransactionTypes().contains(transaction.getStatus());
    }

    protected List<StetTransactionStatus> supportedTransactionTypes() {
        return Arrays.asList(StetTransactionStatus.BOOK, StetTransactionStatus.OTHR);
    }

    protected Instant adjustFetchStartTime(DataRequest dataRequest, Instant fetchStartTime) { //NOSONAR It is provided to customize logic for fetch start time adjustment
        Instant fetchEndTime = dateTimeSupplier.getDefaultInstant().minus(fetchPeriod);
        return fetchStartTime.isBefore(fetchEndTime) ? fetchEndTime : fetchStartTime;
    }
}
