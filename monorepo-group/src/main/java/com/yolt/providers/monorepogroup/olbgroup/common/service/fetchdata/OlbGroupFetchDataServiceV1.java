package com.yolt.providers.monorepogroup.olbgroup.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.olbgroup.common.config.OlbGroupProperties;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.*;
import com.yolt.providers.monorepogroup.olbgroup.common.http.OlbGroupHttpClient;
import com.yolt.providers.monorepogroup.olbgroup.common.http.OlbGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupAccountMapper;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupDateConverter;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;

@RequiredArgsConstructor
public class OlbGroupFetchDataServiceV1 implements OlbGroupFetchDataService {

    private final OlbGroupHttpClientFactory httpClientFactory;
    private final OlbGroupAccountMapper accountMapper;
    private final OlbGroupTransactionMapper transactionMapper;
    private final OlbGroupProviderStateMapper providerStateMapper;
    private final OlbGroupProperties properties;
    private final OlbGroupDateConverter dateConverter;
    private final String providerDisplayName;

    @Override
    public DataProviderResponse fetchAccountsAndTransactions(OlbGroupAuthenticationMeans authMeans,
                                                             AccessMeansDTO accessMeans,
                                                             RestTemplateManager restTemplateManager,
                                                             String psuIpAddress,
                                                             Instant transactionsFetchStartTime) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();
        var httpClient = httpClientFactory.createHttpClient(authMeans, restTemplateManager, providerDisplayName);
        var providerState = providerStateMapper.fromJson(accessMeans.getAccessMeans());
        var consentId = providerState.getConsentId();

        try {
            for (Account account : fetchAccounts(httpClient, consentId, psuIpAddress)) {
                String accountId = account.getResourceId();

                List<Balance> balances = account.getBalances();
                if (CollectionUtils.isEmpty(balances)) {
                    balances = fetchBalances(httpClient, consentId, accountId, psuIpAddress);
                }
                List<ProviderTransactionDTO> transaction = fetchTransactions(httpClient, consentId, accountId, psuIpAddress, transactionsFetchStartTime);

                ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(providerDisplayName, account, balances, transaction);
                providerAccountsDTO.add(providerAccountDTO);

            }

            return new DataProviderResponse(providerAccountsDTO);

        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<Account> fetchAccounts(OlbGroupHttpClient httpClient,
                                        String consentId,
                                        String psuIpAddress) throws TokenInvalidException {
        AccountsResponse response = httpClient.getAccounts(consentId, psuIpAddress);
        if (Objects.isNull(response) || response.getAccounts() == null) {
            throw new MissingDataException("Empty account's response");
        }
        return response.getAccounts();
    }

    private List<Balance> fetchBalances(OlbGroupHttpClient httpClient,
                                        String consentId,
                                        String accountId,
                                        String psuIpAddress) throws TokenInvalidException {
        BalancesResponse response = httpClient.getBalances(accountId, consentId, psuIpAddress);
        if (Objects.isNull(response)  || response.getBalances() == null) {
            throw new MissingDataException("Empty balance's response");
        }
        return response.getBalances();
    }

    private List<ProviderTransactionDTO> fetchTransactions(OlbGroupHttpClient httpClient,
                                                           String consentId,
                                                           String accountId,
                                                           String psuIpAddress,
                                                           Instant transactionsFetchStartTime) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();
        String dateFrom = dateConverter.toDateFormat(transactionsFetchStartTime);

        TransactionsResponse response = httpClient.getTransactions(accountId, consentId, psuIpAddress, dateFrom);
        transactionsDTO.addAll(processTransactionsResponse(response));

        var url = response.getNextHref();
        var pageCounter = 2;
        while (StringUtils.isNotEmpty(url) && (pageCounter <= properties.getPaginationLimit())) {
            response = httpClient.getTransactions(url, consentId, psuIpAddress);
            transactionsDTO.addAll(processTransactionsResponse(response));

            if (!url.equals(response.getNextHref())) {
                url = response.getNextHref();
            }
            else {
                url = null;
            }

            pageCounter++;
        }
        return transactionsDTO;
    }

    private List<ProviderTransactionDTO> processTransactionsResponse(TransactionsResponse response) {
        if (Objects.isNull(response) ) {
            throw new MissingDataException("Empty transaction's response");
        }
        List<ProviderTransactionDTO> transactions = new ArrayList<>();
        transactions.addAll(transactionMapper.mapProviderTransactionsDTO(response.getPendingTransactions(), PENDING));
        transactions.addAll(transactionMapper.mapProviderTransactionsDTO(response.getBookedTransactions(), BOOKED));
        return transactions;
    }
}
