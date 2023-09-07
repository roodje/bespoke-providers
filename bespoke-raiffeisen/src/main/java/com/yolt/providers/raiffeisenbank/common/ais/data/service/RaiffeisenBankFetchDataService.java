package com.yolt.providers.raiffeisenbank.common.ais.data.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClient;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClientFactory;
import com.yolt.providers.raiffeisenbank.common.ais.auth.RaiffeisenBankAuthenticationMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankAccessMeans;
import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Account;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Accounts;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Transactions;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;

@AllArgsConstructor
public class RaiffeisenBankFetchDataService {

    private final RaiffeisenBankHttpClientFactory httpClientFactory;
    private final RaiffeisenBankProperties properties;
    private final RaiffeisenDataMapper dataMapper;

    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                          RaiffeisenBankAuthenticationMeans authMeans,
                                          RaiffeisenBankAccessMeans accessMeans) throws TokenInvalidException, ProviderFetchDataException {
        RaiffeisenBankHttpClient httpClient = httpClientFactory.buildHttpClient(
                authMeans.getTransportKeyId(),
                authMeans.getTlsCertificate(),
                urlFetchData.getRestTemplateManager()
        );

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        String psuIpAddress = urlFetchData.getPsuIpAddress();
        Accounts rawAccounts = httpClient.getAccounts(
                accessMeans.getTokens().getAccessToken(),
                authMeans.getClientId(),
                psuIpAddress,
                accessMeans.getConsentId());
        for (Account account : rawAccounts.getAccounts()) {
            try {
                String accountResourceId = account.getResourceId();
                List<ProviderTransactionDTO> transactions = getAccountTransactions(
                        httpClient,
                        accessMeans,
                        accountResourceId,
                        authMeans.getClientId(),
                        psuIpAddress,
                        urlFetchData.getTransactionsFetchStartTime());

                accounts.add(dataMapper.mapAccountData(account, transactions));
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(accounts);
    }

    private List<ProviderTransactionDTO> getAccountTransactions(
            RaiffeisenBankHttpClient httpClient,
            RaiffeisenBankAccessMeans accessMeans,
            String resourceId,
            String clientId,
            String psuIp,
            Instant transactionsFetchStartTime) throws TokenInvalidException {
        String accessToken = accessMeans.getTokens().getAccessToken();
        String consentId = accessMeans.getConsentId();


        Transactions transactionResponse = httpClient.getAccountTransactions(
                resourceId,
                accessToken,
                clientId,
                psuIp,
                accessMeans.getConsentId(),
                transactionsFetchStartTime);

        List<ProviderTransactionDTO> mappedTransactions = dataMapper.mapTransactions(transactionResponse.getBookedTransactions(), BOOKED);
        mappedTransactions.addAll(dataMapper.mapTransactions(transactionResponse.getPendingTransactions(), PENDING));

        int counter = 1;
        String currentPage = transactionResponse.getNextPageUrl();
        String previousPage = null;

        while (shouldGetNextPage(counter, currentPage, previousPage)) {
            Transactions transactionResponseNextPage = httpClient.getAccountTransactionsNextPage(
                    currentPage,
                    accessToken,
                    clientId,
                    psuIp,
                    consentId);
            mappedTransactions.addAll(dataMapper.mapTransactions(transactionResponseNextPage.getBookedTransactions(), BOOKED));
            mappedTransactions.addAll(dataMapper.mapTransactions(transactionResponseNextPage.getPendingTransactions(), PENDING));
            previousPage = currentPage;
            currentPage = transactionResponse.getNextPageUrl();
            counter++;
        }
        return mappedTransactions;
    }

    private boolean shouldGetNextPage(final int counter,
                                      final String currentPage,
                                      final String previousPage) {
        return counter <= properties.getPaginationLimit() && !Objects.equals(currentPage, previousPage)
                && StringUtils.isNotBlank(currentPage);
    }
}
