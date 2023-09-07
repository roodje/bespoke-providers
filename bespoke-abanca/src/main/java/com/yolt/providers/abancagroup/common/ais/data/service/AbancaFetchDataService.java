package com.yolt.providers.abancagroup.common.ais.data.service;

import com.yolt.providers.abancagroup.abanca.config.AbancaProperties;
import com.yolt.providers.abancagroup.common.AbancaHttpClient;
import com.yolt.providers.abancagroup.common.AbancaHttpClientFactory;
import com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans;
import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaTokens;
import com.yolt.providers.abancagroup.common.ais.data.dto.*;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public class AbancaFetchDataService {

    private final AbancaHttpClientFactory httpClientFactory;
    private final AbancaDataMapper dataMapper;
    private final AbancaProperties properties;
    private final Clock clock;

    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                          AbancaAuthenticationMeans authMeans,
                                          AbancaTokens tokens) throws TokenInvalidException {
        AbancaHttpClient httpClient = httpClientFactory.buildHttpClient(
                authMeans.getTransportKeyId(),
                authMeans.getTlsCertificate(),
                urlFetchData.getRestTemplateManager()
        );

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        UUID apiKey = authMeans.getApiKey();
        Accounts rawAccounts = httpClient.getAccounts(
                tokens.getAccessToken(),
                apiKey,
                authMeans.getSigningCertificateSerialNumber(),
                authMeans.getSigningKeyId(),
                urlFetchData.getSigner(),
                clock);
        for (Account account : rawAccounts.getAccounts()) {
            List<ProviderTransactionDTO> transactions = getAccountTransactions(
                    httpClient,
                    urlFetchData.getSigner(),
                    tokens,
                    apiKey,
                    account.getAccountId(),
                    authMeans.getSigningCertificateSerialNumber(),
                    authMeans.getSigningKeyId(),
                    urlFetchData.getTransactionsFetchStartTime());
            Balance balance = httpClient.getAccountBalances(
                    account.getAccountId(),
                    tokens.getAccessToken(),
                    apiKey,
                    authMeans.getSigningCertificateSerialNumber(),
                    authMeans.getSigningKeyId(),
                    urlFetchData.getSigner(),
                    clock);
            accounts.add(dataMapper.mapAccountData(account, transactions, balance));
        }

        return new DataProviderResponse(accounts);
    }

    private List<ProviderTransactionDTO> getAccountTransactions(
            AbancaHttpClient httpClient,
            Signer signer,
            AbancaTokens abancaTokens,
            UUID apiKey,
            String accountId,
            String signatureKeyId,
            UUID signingKeyId,
            Instant transactionsFetchStartTime) throws TokenInvalidException {

        List<Transaction> rawTransactions = new ArrayList<>();
        Transactions firstPageTransactions = httpClient.getFirstAccountTransactionPage(
                abancaTokens.getAccessToken(),
                apiKey,
                accountId,
                transactionsFetchStartTime,
                signatureKeyId,
                signingKeyId,
                signer,
                clock);
        rawTransactions.addAll(firstPageTransactions.getTransactions());

        int counter = 1;
        String currentPage = firstPageTransactions.getNextPageUrl();
        String previousPage = null;

        while (checkPaginationLimitAndIfNextPageIsAvailable(counter, currentPage, previousPage)) {
            Transactions nextPageTransactions = httpClient.getNextAccountTransactionsNextPage(
                    currentPage,
                    abancaTokens.getAccessToken(),
                    apiKey,
                    signatureKeyId,
                    signingKeyId,
                    signer,
                    clock);
            rawTransactions.addAll(nextPageTransactions.getTransactions());
            previousPage = currentPage;
            currentPage = nextPageTransactions.getNextPageUrl();
            counter++;
        }
        return dataMapper.mapTransactions(rawTransactions);
    }

    private boolean checkPaginationLimitAndIfNextPageIsAvailable(final int counter,
                                                                 final String currentPage,
                                                                 final String previousPage) {
        return counter <= properties.getPaginationLimit() && !Objects.equals(currentPage, previousPage)
                && StringUtils.isNotBlank(currentPage);
    }
}
