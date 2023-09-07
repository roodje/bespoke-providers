package com.yolt.providers.kbcgroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans;
import com.yolt.providers.kbcgroup.common.KbcGroupProperties;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupAccessMeans;
import com.yolt.providers.kbcgroup.common.exception.KbcGroupHttpErrorHandler;
import com.yolt.providers.kbcgroup.common.mapper.KbcGroupAccountMapper;
import com.yolt.providers.kbcgroup.common.mapper.KbcGroupTransactionMapper;
import com.yolt.providers.kbcgroup.common.rest.KbcGroupHttpClient;
import com.yolt.providers.kbcgroup.common.rest.KbcGroupRestTemplateService;
import com.yolt.providers.kbcgroup.dto.*;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KbcGroupFetchDataService {

    private final KbcGroupRestTemplateService restTemplateService;
    private final KbcGroupAccountMapper accountMapper;
    private final KbcGroupProperties properties;

    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                          KbcGroupAuthMeans authMeans,
                                          KbcGroupAccessMeans accessMeans) throws TokenInvalidException, ProviderFetchDataException {
        KbcGroupHttpClient httpClient = restTemplateService.createHttpClient(authMeans, urlFetchData.getRestTemplateManager());

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        String psuIpAddress = urlFetchData.getPsuIpAddress();
        String accountResourceId = null;
        try {
            InlineResponse200 kbcAccountsResponse = httpClient.getAccounts(
                    accessMeans.getKbcGroupTokenResponse().getAccessToken(),
                    accessMeans.getConsentId(),
                    psuIpAddress
            );

            for (InlineResponse200Accounts account : kbcAccountsResponse.getAccounts()) {

                accountResourceId = account.getResourceId();
                List<ProviderTransactionDTO> transactions = getAccountTransactions(httpClient,
                        accessMeans,
                        accountResourceId,
                        urlFetchData.getTransactionsFetchStartTime(),
                        psuIpAddress);

                accounts.add(accountMapper.toProviderAccountDTO(account, getBalance(account.getBalances()), transactions));
            }
        } catch (HttpStatusCodeException e) {
            KbcGroupHttpErrorHandler.handleNon2xxResponseCodeForFetchData(e.getStatusCode());
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }

        return new DataProviderResponse(accounts);
    }

    private Balance1 getBalance(List<Balance1> balances) {
        // According to documentation: "Only closingBooked is currently supported by KBC."
        if (balances.size() == 1) {
            Balance1 balance = balances.get(0);
            if (balance.getBalanceType() != null && balance.getBalanceAmount() != null) {
                return balance;
            }
        }
        return null;
    }

    private List<ProviderTransactionDTO> getAccountTransactions(KbcGroupHttpClient httpClient,
                                                                KbcGroupAccessMeans accessMeans,
                                                                String resourceId,
                                                                Instant transactionsFetchStartTime,
                                                                String psuIpAddress) {
        String accessToken = accessMeans.getKbcGroupTokenResponse().getAccessToken();
        String consentId = accessMeans.getConsentId();

        List<ProviderTransactionDTO> mappedTransactions = new ArrayList<>();

        InlineResponse2003 kbcAccountTransactionsResponse = httpClient.getAccountTransactions(accessToken,
                consentId,
                resourceId,
                transactionsFetchStartTime,
                psuIpAddress);

        mapTransactions(kbcAccountTransactionsResponse.getTransactions().getBooked(), mappedTransactions);

        int counter = 1;
        String currentPage = retrieveNextPageLink(kbcAccountTransactionsResponse.getTransactions().getLinks());
        String previousPage = null;

        while (checkPaginationLimitAndIfNextPageIsAvailable(counter, currentPage, previousPage)) {
            InlineResponse2003 kbcTransactionsNextPage = httpClient.getNextPageOfAccountTransactions(accessToken, consentId, psuIpAddress, currentPage);
            mapTransactions(kbcTransactionsNextPage.getTransactions().getBooked(), mappedTransactions);
            previousPage = currentPage;
            currentPage = retrieveNextPageLink(kbcTransactionsNextPage.getTransactions().getLinks());
            counter++;
        }
        return mappedTransactions;
    }

    private String retrieveNextPageLink(AccountReport1Links transactionLinks) {
        if (transactionLinks == null || StringUtils.isBlank(transactionLinks.getNext())) {
            return null;
        }
        return transactionLinks.getNext();
    }

    private boolean checkPaginationLimitAndIfNextPageIsAvailable(final int counter,
                                                                 final String currentPage,
                                                                 final String previousPage) {
        return counter <= properties.getPaginationLimit() && !Objects.equals(currentPage, previousPage)
                && StringUtils.isNotBlank(currentPage);
    }

    private void mapTransactions(List<Transaction1> bookedTransactions, List<ProviderTransactionDTO> mappedTransactions) {
        mappedTransactions.addAll(
                bookedTransactions.stream()
                        .map(KbcGroupTransactionMapper::toProviderTransactionDto)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
    }
}