package com.yolt.providers.direkt1822group.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.direkt1822group.common.Direkt1822GroupAccessMeans;
import com.yolt.providers.direkt1822group.common.Direkt1822GroupAuthenticationMeans;
import com.yolt.providers.direkt1822group.common.dto.AccountsResponse;
import com.yolt.providers.direkt1822group.common.dto.BalancesResponse;
import com.yolt.providers.direkt1822group.common.dto.Transactions;
import com.yolt.providers.direkt1822group.common.rest.Direkt1822GroupHttpClient;
import com.yolt.providers.direkt1822group.common.rest.Direkt1822RestTemplateService;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Direkt1822GroupFetchDataService {

    private final Direkt1822RestTemplateService restTemplateService;
    private final Direkt1822GroupMapperService mapperService;
    private final Clock clock;

    public DataProviderResponse fetchData(final String provider,
                                          final Direkt1822GroupAccessMeans accessMeans,
                                          final Direkt1822GroupAuthenticationMeans authenticationMeans,
                                          final RestTemplateManager restTemplateManager,
                                          final Instant transactionFetchStartTime,
                                          final String psuIpAddress)
            throws ProviderFetchDataException, TokenInvalidException {

        Direkt1822GroupHttpClient httpClient = restTemplateService.createHttpClient(authenticationMeans,
                restTemplateManager,
                provider,
                clock);

        final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        List<ProviderAccountDTO> providerAccountDTOS = fetchAccounts(httpClient, accessMeans, psuIpAddress);
        for (ProviderAccountDTO accountDTO : providerAccountDTOS) {
            try {
                ProviderAccountDTO filledAccount = fetchAccountData(accessMeans, httpClient, accountDTO, transactionFetchStartTime, psuIpAddress);
                responseAccounts.add(filledAccount);
            } catch (RuntimeException e) {
                if (e instanceof BackPressureRequestException) {
                    throw e;
                }
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(responseAccounts);
    }

    private List<ProviderAccountDTO> fetchAccounts(Direkt1822GroupHttpClient httpClient,
                                                   Direkt1822GroupAccessMeans accessMeans,
                                                   String psuIpAddress) throws TokenInvalidException {
        AccountsResponse accounts = httpClient.fetchAccounts(accessMeans.getConsentId(), psuIpAddress);

        return accounts.getAccounts()
                .stream()
                .map(mapperService::createProviderAccountDTO)
                .collect(Collectors.toList());
    }

    private ProviderAccountDTO fetchAccountData(Direkt1822GroupAccessMeans accessMeans, final Direkt1822GroupHttpClient httpClient,
                                                final ProviderAccountDTO account,
                                                final Instant transactionsFetchStartTime,
                                                String psuIpAddress) throws TokenInvalidException {
        String accountId = account.getAccountId();
        BalancesResponse accountBalances = httpClient.fetchBalances(accessMeans.getConsentId(), accountId, psuIpAddress);
        ProviderAccountDTO accountDTO = mapperService.updateAccountDTOWithBalances(account, accountBalances);

        Transactions transactions = httpClient.fetchTransactions(accessMeans.getConsentId(), accountId, transactionsFetchStartTime, psuIpAddress);
        List<ProviderTransactionDTO> transactionDTO = mapperService.mapToProviderTransactionDTO(transactions);
        accountDTO.getTransactions().addAll(transactionDTO);
        return accountDTO;
    }
}
