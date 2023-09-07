package com.yolt.providers.knabgroup.common.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.auth.KnabSigningService;
import com.yolt.providers.knabgroup.common.dto.external.Accounts;
import com.yolt.providers.knabgroup.common.dto.external.Balances;
import com.yolt.providers.knabgroup.common.dto.external.Transactions;
import com.yolt.providers.knabgroup.common.dto.internal.FetchDataResultV2;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import com.yolt.providers.knabgroup.common.exception.KnabGroupFetchDataException;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClient;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.http.HttpEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class KnabGroupFetchDataServiceV2 {

    private static final DateTimeFormatter TRANSACTION_FROM_TIME_FORMATTER_PARAMETER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private final KnabGroupHttpClientFactory httpClientFactory;
    private final KnabSigningService signingService;
    private final KnabGroupMapperServiceV2 mapperService;
    private final Clock clock;

    public DataProviderResponse fetchData(final KnabAccessMeans accessMeans,
                                          final KnabGroupAuthenticationMeans authenticationMeans,
                                          final RestTemplateManager restTemplateManager,
                                          final Signer signer,
                                          final String psuIpAddress,
                                          final Instant transactionFetchStartTime)
            throws ProviderFetchDataException, TokenInvalidException {
        KnabFetchDataHeaders headers = new KnabFetchDataHeaders(signingService, Instant.now(clock), accessMeans, authenticationMeans.getSigningData(signer), Optional.ofNullable(psuIpAddress));
        KnabGroupHttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authenticationMeans);
        List<ProviderAccountDTO> userAccountList = getAccounts(httpClient, accessMeans, authenticationMeans, signer, psuIpAddress);
        FetchDataResultV2 result = fetchData(userAccountList, httpClient, headers, transactionFetchStartTime);
        return new DataProviderResponse(result.getResponseAccounts());
    }

    public List<ProviderAccountDTO> getAccounts(final KnabGroupHttpClient httpClient, final KnabAccessMeans accessMeans, final KnabGroupAuthenticationMeans authenticationMeans, final Signer signer, final String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        try {
            KnabFetchDataHeaders headers = new KnabFetchDataHeaders(signingService, Instant.now(clock), accessMeans, authenticationMeans.getSigningData(signer), Optional.ofNullable(psuIpAddress));
            HttpEntity request = new HttpEntity<>(headers);
            Accounts accounts = httpClient.fetchAccounts(request);
            if (accounts == null || accounts.getData() == null) {
                throw new GetAccessTokenFailedException("No accounts retrieved for a user");
            }
            return accounts.getData()
                    .stream()
                    .map(mapperService::createProviderAccountDTO)
                    .collect(Collectors.toList());
        } catch (KnabGroupFetchDataException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private FetchDataResultV2 fetchData(final List<ProviderAccountDTO> accounts, final KnabGroupHttpClient httpClient, final KnabFetchDataHeaders headers, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException {
        FetchDataResultV2 result = new FetchDataResultV2();
        for (ProviderAccountDTO account : accounts) {//NOSONAR restTemplate has internal nonNull assert assuring result is not null
            try {
                ProviderAccountDTO accountDTO = fetchAccountData(httpClient, headers, account, transactionsFetchStartTime);
                result.addFetchedAccount(accountDTO);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return result;
    }

    private ProviderAccountDTO fetchAccountData(final KnabGroupHttpClient httpClient, KnabFetchDataHeaders headers, final ProviderAccountDTO account, final Instant transactionsFetchStartTime) throws TokenInvalidException {
        String accountId = account.getAccountId();
        HttpEntity balancesAndTransactionsRequest = new HttpEntity<>(headers);
        Balances accountBalances = httpClient.fetchBalances(balancesAndTransactionsRequest, accountId);
        ProviderAccountDTO accountDTO = mapperService.updateProviderAccountDTO(account, accountBalances, clock);
        Transactions transactions = httpClient.fetchTransactions(balancesAndTransactionsRequest, calculateDateFromParameter(transactionsFetchStartTime), accountId);
        List<ProviderTransactionDTO> transactionDTO = mapperService.mapToProviderTransactionDTO(transactions);
        accountDTO.getTransactions().addAll(transactionDTO);
        return accountDTO;
    }

    private String calculateDateFromParameter(final Instant transactionsFetchStartTime) {
        return transactionsFetchStartTime != null ? TRANSACTION_FROM_TIME_FORMATTER_PARAMETER.format(Instant.ofEpochSecond(transactionsFetchStartTime.getEpochSecond())) : "";
    }

}
