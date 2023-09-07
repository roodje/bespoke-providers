package com.yolt.providers.bancacomercialaromana.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancacomercialaromana.common.configuration.BcrGroupProperties;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHttpClient;
import com.yolt.providers.bancacomercialaromana.common.mapper.BcrGroupAccountMapper;
import com.yolt.providers.bancacomercialaromana.common.mapper.BcrGroupTransactionMapper;
import com.yolt.providers.bancacomercialaromana.common.model.Token;
import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Account;
import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Balance;
import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.Transaction;
import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.TransactionsResponse;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans.WEB_API_KEY_NAME;
import static com.yolt.providers.bancacomercialaromana.common.util.BcrGroupDateUtil.toNarrowedDateFormat;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;

@Slf4j
@RequiredArgsConstructor
public class BcrGroupFetchDataServiceV1 {
    private static final String TRANSACTIONS_TEMPLATE = "/v1/accounts/{accountId}/transactions";

    private final BcrGroupAccountMapper accountMapper;
    private final BcrGroupTransactionMapper transactionMapper;
    private final BcrGroupProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public DataProviderResponse fetchData(BcrGroupHttpClient httpClient,
                                          UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();

        Token providerState = toOAuthToken(request.getAccessMeans().getAccessMeans());
        String webApiKey = request.getAuthenticationMeans().get(WEB_API_KEY_NAME).getValue();
        String accessToken = providerState.getAccessToken();
        List<Account> accounts = fetchAccounts(httpClient, accessToken, webApiKey);

        for (Account account : accounts) {
            String accountId = account.getResourceId();
            try {
                List<Balance> balances = fetchBalances(httpClient, accessToken, accountId, webApiKey);
                List<ProviderTransactionDTO> transaction = fetchTransactions(
                        httpClient,
                        request.getTransactionsFetchStartTime(),
                        accessToken,
                        accountId,
                        webApiKey);

                ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(account, balances, transaction);
                providerAccountsDTO.add(providerAccountDTO);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private List<Account> fetchAccounts(BcrGroupHttpClient httpClient,
                                        String accessToken,
                                        String webApiKey) throws TokenInvalidException, ProviderFetchDataException {
        try {
            return httpClient
                    .getAccounts(accessToken, webApiKey)
                    .getAccounts();
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<Balance> fetchBalances(BcrGroupHttpClient httpClient,
                                        String accessToken,
                                        String accountId,
                                        String webApiKey) throws TokenInvalidException {

        return httpClient
                .getBalances(accessToken, webApiKey, accountId)
                .getBalances();
    }

    private List<ProviderTransactionDTO> fetchTransactions(BcrGroupHttpClient httpClient,
                                                           Instant transactionsFetchStartTime,
                                                           String accessToken,
                                                           String accountId,
                                                           String webApiKey) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();

        String url = UriComponentsBuilder.fromUriString(TRANSACTIONS_TEMPLATE)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", toNarrowedDateFormat(clock, transactionsFetchStartTime, 89))
                .buildAndExpand(accountId)
                .toUriString();

        int pageCounter = 1;
        while (StringUtils.isNotEmpty(url) && (pageCounter <= properties.getPaginationLimit())) {
            TransactionsResponse response = httpClient.getTransactions(url, accessToken, webApiKey);
            List<Transaction> pendingTransactions = response.getPendingTransactions();
            List<Transaction> bookedTransactions = response.getBookedTransactions();
            if (pendingTransactions != null) {
                transactionsDTO.addAll(transactionMapper.mapProviderTransactionsDTO(pendingTransactions, PENDING));
            }
            if (bookedTransactions != null) {
                transactionsDTO.addAll(transactionMapper.mapProviderTransactionsDTO(bookedTransactions, BOOKED));
            }

            url = response.getNextHref();
            pageCounter++;
        }
        return transactionsDTO;
    }

    private Token toOAuthToken(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, Token.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Couldn't parse access means to Token object", e);
        }
    }
}
