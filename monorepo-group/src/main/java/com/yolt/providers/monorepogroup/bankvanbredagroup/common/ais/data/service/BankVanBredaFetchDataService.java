package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupAccessMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Account;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Accounts;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Transactions;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.service.BankVanBredaGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.config.BankVanBredaGroupProperties;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class BankVanBredaFetchDataService {

    private final BankVanBredaGroupHttpClientFactory httpClientFactory;
    private final BankVanBredaDataMapper dataMapper;
    private final BankVanBredaGroupProperties properties;

    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                          BankVanBredaGroupAuthenticationMeans authMeans,
                                          BankVanBredaGroupAccessMeans accessMeans,
                                          String psuIp) throws TokenInvalidException, ProviderFetchDataException {
        try {
            BankVanBredaGroupDataHttpClient httpClient = httpClientFactory.buildDataHttpClient(
                    authMeans.getTransportKeyId(),
                    authMeans.getTlsCertificate(),
                    urlFetchData.getRestTemplateManager()
            );

            List<ProviderAccountDTO> accounts = new ArrayList<>();

            Accounts rawAccounts = httpClient.getAccounts(
                    accessMeans.getConsentId(),
                    psuIp,
                    accessMeans.getTokens().getAccessToken());
            for (Account account : rawAccounts.getAccounts()) {
                List<ProviderTransactionDTO> transactions = getAccountTransactions(
                        httpClient,
                        account.getResourceId(),
                        accessMeans.getTokens().getAccessToken(),
                        accessMeans.getConsentId(),
                        psuIp,
                        urlFetchData.getTransactionsFetchStartTime());
                accounts.add(dataMapper.mapAccountData(account, transactions));
            }

            return new DataProviderResponse(accounts);

        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<ProviderTransactionDTO> getAccountTransactions(BankVanBredaGroupDataHttpClient httpClient,
                                                                String accountId,
                                                                String accessToken,
                                                                String consentId,
                                                                String psuIp,
                                                                Instant transactionsFetchStartTime) throws TokenInvalidException {

        Transactions firstPageTransactions = httpClient.getAccountTransactions(
                accountId,
                consentId,
                psuIp,
                accessToken,
                transactionsFetchStartTime);
        List<ProviderTransactionDTO> mappedTransactions = dataMapper.mapTransactions(firstPageTransactions);

        int counter = 1;
        String currentPage = firstPageTransactions.getNextPageUrl();
        String previousPage = null;

        while (checkPaginationLimitAndIfNextPageIsAvailable(counter, currentPage, previousPage)) {
            Transactions nextPageTransactions = httpClient.getNextAccountTransactionsNextPage(
                    currentPage,
                    consentId,
                    psuIp,
                    accessToken);
            mappedTransactions.addAll(dataMapper.mapTransactions(nextPageTransactions));
            previousPage = currentPage;
            currentPage = nextPageTransactions.getNextPageUrl();
            counter++;
        }
        return mappedTransactions;
    }

    private boolean checkPaginationLimitAndIfNextPageIsAvailable(final int counter,
                                                                 final String currentPage,
                                                                 final String previousPage) {
        return counter <= properties.getPaginationLimit() && !Objects.equals(currentPage, previousPage)
                && StringUtils.isNotBlank(currentPage);
    }
}
