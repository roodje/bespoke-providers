package com.yolt.providers.n26.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.n26.common.config.BaseN26Properties;
import com.yolt.providers.n26.common.consentwindow.ConsentWindow;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.dto.ais.fetchdata.*;
import com.yolt.providers.n26.common.http.N26GroupHttpClient;
import com.yolt.providers.n26.common.service.mapper.N26GroupAccountMapper;
import com.yolt.providers.n26.common.service.mapper.N26GroupProviderStateMapper;
import com.yolt.providers.n26.common.service.mapper.N26GroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yolt.providers.n26.common.util.N26GroupDateUtil.toDateFormat;

@RequiredArgsConstructor
public class N26GroupFetchDataService {

    private static final String SUPPORTED_ACCOUNT_STATUS = "enabled";

    private final N26GroupAccountMapper accountMapper;
    private final N26GroupTransactionMapper transactionMapper;
    private final N26GroupProviderStateMapper providerStateMapper;
    private final ConsentWindow consentWindow;
    private final BaseN26Properties properties;
    private final Clock clock;


    public DataProviderResponse fetchData(N26GroupHttpClient httpClient,
                                          UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();

        N26GroupProviderState providerState = providerStateMapper.fromAccessMeansDTO(request.getAccessMeans());

        for (Account account : fetchAccounts(httpClient, request, providerState)) {
            String accountId = account.getResourceId();
            try {
                if (SUPPORTED_ACCOUNT_STATUS.equals(account.getStatus())) {
                    List<Balance> balances = fetchBalances(httpClient, accountId, request.getPsuIpAddress(), providerState);
                    List<ProviderTransactionDTO> transaction = fetchTransactions(httpClient, request, providerState, accountId);

                    ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(account, balances, transaction);
                    providerAccountsDTO.add(providerAccountDTO);
                }
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private List<Account> fetchAccounts(N26GroupHttpClient httpClient,
                                        UrlFetchDataRequest request,
                                        N26GroupProviderState providerState) throws TokenInvalidException, ProviderFetchDataException {
        try {
            AccountsResponse accountsResponse = httpClient.getAccounts(providerState, request.getPsuIpAddress());
            return accountsResponse.getAccounts();
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<Balance> fetchBalances(N26GroupHttpClient httpClient,
                                        String accountId,
                                        String psuIpAddress,
                                        N26GroupProviderState providerState) throws TokenInvalidException, ProviderFetchDataException {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(httpClient.getBalancesTemplatePath())
                    .buildAndExpand(accountId)
                    .toUriString();

            BalancesResponse balancesResponse = httpClient.getBalances(url, providerState, psuIpAddress);
            return balancesResponse.getBalances();
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<ProviderTransactionDTO> fetchTransactions(N26GroupHttpClient httpClient,
                                                           UrlFetchDataRequest request,
                                                           N26GroupProviderState providerState,
                                                           String accountId) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();

        Instant fetchDataStartDate = consentWindow.whenFromToFetchData(providerState.getConsentGeneratedAt(), request.getTransactionsFetchStartTime(), clock);

        String url = UriComponentsBuilder.fromUriString(httpClient.getTransactionsTemplatePath())
                .queryParam("bookingStatus", "booked")
                .queryParam("dateFrom", toDateFormat(fetchDataStartDate))
                .buildAndExpand(accountId)
                .toUriString();

        int pageCounter = 1;
        while (StringUtils.isNotEmpty(url) && (pageCounter <= properties.getPaginationLimit())) {
            TransactionsResponse response = httpClient.getTransactions(url, providerState, request.getPsuIpAddress());
            transactionsDTO.addAll(transactionMapper.mapProviderTransactionsDTO(response.getBookedTransactions()));

            url = response.getNextHref();
            pageCounter++;
        }
        return transactionsDTO;
    }
}
