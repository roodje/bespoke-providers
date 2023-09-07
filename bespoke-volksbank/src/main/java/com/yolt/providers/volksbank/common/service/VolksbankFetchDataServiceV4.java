package com.yolt.providers.volksbank.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import com.yolt.providers.volksbank.common.model.VolksbankAccessMeans;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientV4;
import com.yolt.providers.volksbank.common.service.mapper.VolksbankDataMapperService;
import com.yolt.providers.volksbank.dto.v1_1.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;

@Slf4j
@RequiredArgsConstructor
public class VolksbankFetchDataServiceV4 {

    private final VolksbankBaseProperties properties;
    private final VolksbankDataMapperService mapperService;

    public DataProviderResponse fetchData(final VolksbankAccessMeans accessMeans,
                                          final Instant transactionsFetchStartTime,
                                          final String providerName,
                                          final VolksbankHttpClientV4 httpClient) throws ProviderFetchDataException, TokenInvalidException {

        String accessToken = accessMeans.getResponse().getAccessToken();
        String consentId = accessMeans.getConsentId();

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        AccountResponse allUserAccounts = httpClient.getAllUserAccounts(
                accessMeans.getResponse().getAccessToken(), accessMeans.getConsentId());

        for (AccountDetails account : allUserAccounts.getAccounts()) {
            try {
                List<ProviderTransactionDTO> transactionsConverted = getTransactionsForAccount(httpClient, accessMeans, account, transactionsFetchStartTime);
                BalanceResponse balances = httpClient.getBalanceForAccount(accessToken, consentId, account.getResourceId());
                accounts.add(mapperService.mapToProviderAccountDTO(account, getBalance(balances), transactionsConverted, providerName));
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(accounts);
    }

    private List<ProviderTransactionDTO> getTransactionsForAccount(final VolksbankHttpClientV4 httpClient,
                                                                   final VolksbankAccessMeans accessMeans,
                                                                   final AccountDetails account,
                                                                   final Instant transactionsFetchStartTime) throws TokenInvalidException {
        String accessToken = accessMeans.getResponse().getAccessToken();
        String consentId = accessMeans.getConsentId();

        List<ProviderTransactionDTO> transactionsConverted = new ArrayList<>();

        TransactionResponse userTransactionForGivenAccount = httpClient.getUserTransactionForGivenAccount(
                accessToken, consentId, account.getResourceId(), transactionsFetchStartTime);

        mapTransactionObjectAndAddToProviderTransactionList(userTransactionForGivenAccount, transactionsConverted);

        int counter = 1;
        String currentPage = getLinkForNextTransaction(userTransactionForGivenAccount);
        String previousPage = null;

        while (checkPaginationLimitAndIfNextPageIsAvailable(counter, currentPage, previousPage)) {
            TransactionResponse nextUserTransactionForGivenAccount = httpClient.getUserTransactionForGivenAccount(accessToken, consentId, currentPage);
            mapTransactionObjectAndAddToProviderTransactionList(nextUserTransactionForGivenAccount, transactionsConverted);
            previousPage = currentPage;
            currentPage = getLinkForNextTransaction(nextUserTransactionForGivenAccount);
            counter++;
        }
        return transactionsConverted;
    }

    private boolean checkPaginationLimitAndIfNextPageIsAvailable(final int counter,
                                                                 final String currentPage,
                                                                 final String previousPage) {
        return counter <= properties.getPaginationLimit() && !Objects.equals(currentPage, previousPage)
                && StringUtils.isNotBlank(currentPage);
    }

    private BalanceItem getBalance(BalanceResponse balances) {
        //According to documentation de Volksbank only support one interim available balance
        if (balances.getBalances().size() == 1) {
            BalanceItem balance = balances.getBalances().get(0);
            if (balance.getBalanceType() != null && balance.getBalanceAmount() != null
                    && balance.getLastChangeDateTime() != null) {
                return balance;
            }
        }

        throw new IllegalStateException("Returned balance was not correct");
    }

    private void mapTransactionObjectAndAddToProviderTransactionList(final TransactionResponse userTransactionForGivenAccount,
                                                                     final List<ProviderTransactionDTO> transactionsConverted) {
        userTransactionForGivenAccount.getTransactions().getBooked()
                .stream()
                .map(mapperService::mapToProviderTransactionDTO)
                .filter(Objects::nonNull)
                .forEach(transactionsConverted::add);
    }

    private String getLinkForNextTransaction(final TransactionResponse transaction) {
        return Optional.of(transaction)
                .map(TransactionResponse::getTransactions)
                .map(AccountReport::getLinks)
                .map(Links::getNext)
                .map(Link::getHref)
                //Bank may return the following url: /psd2/snsbank/v1.1/accounts.... that is a part our base url, so trim it
                .map(url -> url.substring(url.indexOf("/v1.1/account")))
                .orElse(StringUtils.EMPTY);
    }
}
