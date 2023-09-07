package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.CommonProperties;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.argentagroup.dto.*;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
public class DefaultTransactionsDataFetchService implements TransactionsDataFetchService {

    public static final String ACCOUNT_ID_PARAMETER_NAME = "accountId";
    public static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";
    public static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    public static final String BOOKING_STATUS_VALUE = "both";


    private final String transactionsEndpointPath;
    private final CommonProperties properties;
    private final FetchDataHttpHeadersProvider fetchDataHttpHeadersProvider;
    private final TransactionsMapper transactionsMapper;
    private final HttpErrorHandler transactionsHttpErrorHandler;

    @Override
    public List<ProviderTransactionDTO> getTransactions(final UrlFetchDataRequest request,
                                                        final DefaultAuthenticationMeans authenticationMeans,
                                                        final HttpClient httpClient,
                                                        final ProviderAccountDTO account,
                                                        final AccessMeans accessMeans) throws TokenInvalidException {
        String dateFrom = LocalDate.ofInstant(request.getTransactionsFetchStartTime(), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);

        HttpHeaders requestHeaders = fetchDataHttpHeadersProvider.providerHeaders(
                request, authenticationMeans, accessMeans);

        String uri = UriComponentsBuilder.fromPath(transactionsEndpointPath)
                .uriVariables(Map.of(ACCOUNT_ID_PARAMETER_NAME, account.getAccountId()))
                .queryParam(BOOKING_STATUS_PARAMETER_NAME, BOOKING_STATUS_VALUE)
                .queryParam(DATE_FROM_PARAMETER_NAME, dateFrom)
                .toUriString();

        GetTransactionsResponse response = httpClient.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(requestHeaders),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                GetTransactionsResponse.class,
                transactionsHttpErrorHandler
        ).getBody();

        List<GetTransactionsResponseTransactionsBooked> bookedTransactions = new ArrayList<>(extractBookedTransactions(response));
        List<GetTransactionsResponseTransactionsBooked> pendingTransactions = new ArrayList<>(extractPendingTransactions(response));

        int pageNumber = 1;
        String nextPage = extractNextPage(response);
        while (StringUtils.isNotBlank(nextPage) && pageNumber <= properties.getPaginationLimit()) {
            requestHeaders = fetchDataHttpHeadersProvider.providerHeaders(
                    request, authenticationMeans, accessMeans);

            response = httpClient.exchange(
                    nextPage,
                    HttpMethod.GET,
                    new HttpEntity<>(requestHeaders),
                    ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                    GetTransactionsResponse.class,
                    transactionsHttpErrorHandler
            ).getBody();

            bookedTransactions.addAll(extractBookedTransactions(response));
            pendingTransactions.addAll(extractPendingTransactions(response));

            nextPage = extractNextPage(response);
            pageNumber++;
        }

        List<ProviderTransactionDTO> transactions = new ArrayList<>();

        bookedTransactions.stream()
                .map(transactionsMapper::mapBookedTransaction)
                .forEach(transactions::add);

        pendingTransactions.stream()
                .map(transactionsMapper::mapPendingTransaction)
                .forEach(transactions::add);

        return transactions;
    }

    private String extractNextPage(final GetTransactionsResponse response) {
        return Optional.of(response)
                .map(GetTransactionsResponse::getTransactions)
                .map(GetTransactionsResponseTransactions::getLinks)
                .map(GetTransactionsResponseTransactionsLinks::getNext)
                .map(GetTransactionsResponseTransactionsLinksNext::getHref)
                .orElse(null);
    }

    private List<GetTransactionsResponseTransactionsBooked> extractBookedTransactions(final GetTransactionsResponse responseTransactions) {
        return Optional.of(responseTransactions)
                .map(GetTransactionsResponse::getTransactions)
                .map(GetTransactionsResponseTransactions::getBooked)
                .orElse(Collections.emptyList());
    }

    private List<GetTransactionsResponseTransactionsBooked> extractPendingTransactions(final GetTransactionsResponse responseTransactions) {
        return Optional.of(responseTransactions)
                .map(GetTransactionsResponse::getTransactions)
                .map(GetTransactionsResponseTransactions::getPending)
                .orElse(Collections.emptyList());
    }
}
