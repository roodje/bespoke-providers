package com.yolt.providers.consorsbankgroup.common.ais.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.consorsbankgroup.common.ais.DefaultAccessMeans;
import com.yolt.providers.consorsbankgroup.common.ais.http.DefaultRestClient;
import com.yolt.providers.consorsbankgroup.common.ais.mapper.DefaultAccountMapper;
import com.yolt.providers.consorsbankgroup.common.ais.mapper.DefaultBalanceMapper;
import com.yolt.providers.consorsbankgroup.common.ais.mapper.DefaultTransactionMapper;
import com.yolt.providers.consorsbankgroup.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DefaultFetchDataService {

    private final DefaultAccountMapper accountMapper;
    private final DefaultBalanceMapper balanceMapper;
    private final DefaultTransactionMapper transactionMapper;
    private final DefaultRestClient restClient;

    public DataProviderResponse fetchData(final DefaultAccessMeans accessMeans,
                                          final LocalDate transactionsFetchStartDate,
                                          final HttpClient httpClient,
                                          final String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        List<ProviderAccountDTO> accounts = new ArrayList<>();

        String consentId = accessMeans.getConsentId();

        AccountList accountsResponse = restClient.getAccounts(consentId, psuIpAddress, httpClient);

        for (AccountDetails account : accountsResponse.getAccounts()) {
            try {
                if (accountMapper.mapAccountType(account) != null) {
                    List<ProviderTransactionDTO> transactionDTOS = getTransactionsForAccount(account.getResourceId(),
                            consentId, transactionsFetchStartDate, httpClient, psuIpAddress);
                    Map<BalanceType, BalanceDTO> balanceMap = getBalancesForAccount(account.getResourceId(), consentId, httpClient, psuIpAddress);

                    ProviderAccountDTO providerAccountDTO = accountMapper.mapAccount(account, transactionDTOS, balanceMap);
                    accounts.add(providerAccountDTO);
                }
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(accounts);
    }

    private Map<BalanceType, BalanceDTO> getBalancesForAccount(final String accountId,
                                                               final String consentId,
                                                               final HttpClient httpClient,
                                                               final String psuIpAddress) throws TokenInvalidException {
        ReadAccountBalanceResponse200 balanceResponse = restClient.getBalances(accountId, consentId, psuIpAddress, httpClient);
        return balanceMapper.mapBalances(balanceResponse.getBalances());
    }


    private List<ProviderTransactionDTO> getTransactionsForAccount(final String accountId,
                                                                   final String consentId,
                                                                   final LocalDate transactionsFetchStartDate,
                                                                   final HttpClient httpClient,
                                                                   final String psuIpAddress) throws TokenInvalidException {
        TransactionsResponse200Json transactionsResponse = restClient.getFirstPageOfTransactions(accountId, consentId,
                psuIpAddress, transactionsFetchStartDate, httpClient);
        List<ProviderTransactionDTO> mappedTransactions = transactionMapper.mapTransactions(transactionsResponse);

        Optional<String> nextPage = extractNextPage(transactionsResponse, null);

        while (nextPage.isPresent()) {
            transactionsResponse = restClient.getPageOfTransactions(consentId, psuIpAddress, nextPage.get(), httpClient);
            mappedTransactions.addAll(transactionMapper.mapTransactions(transactionsResponse));
            nextPage = extractNextPage(transactionsResponse, nextPage.get());
        }

        return mappedTransactions;
    }

    private Optional<String> extractNextPage(final TransactionsResponse200Json transactionsResponse, final String currentPage) {
        return Optional.ofNullable(transactionsResponse)
                .map(TransactionsResponse200Json::getTransactions)
                .map(AccountReport::getLinks)
                .map(links -> links.get("next"))
                .map(HrefType::getHref)
                .filter(StringUtils::hasText)
                .filter(next -> !next.equals(currentPage));
    }
}
