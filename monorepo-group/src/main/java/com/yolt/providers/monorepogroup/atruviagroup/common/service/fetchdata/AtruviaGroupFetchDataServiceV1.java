package com.yolt.providers.monorepogroup.atruviagroup.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.atruviagroup.common.AtruviaGroupProperties;
import com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaAccessMeans;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupAccountMapper;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupDateConverter;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.atruviagroup.common.mapper.AtruviaGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class AtruviaGroupFetchDataServiceV1 implements AtruviaGroupFetchDataService {

    private final AtruviaGroupHttpClientFactory httpClientFactory;
    private final AtruviaGroupAccountMapper accountMapper;
    private final AtruviaGroupTransactionMapper transactionMapper;
    private final AtruviaGroupProviderStateMapper providerStateMapper;
    private final AtruviaGroupProperties properties;
    private final AtruviaGroupDateConverter dateConverter;
    private final String providerDisplayName;

    @Override
    public DataProviderResponse fetchAccountsAndTransactions(AtruviaGroupAuthenticationMeans authMeans,
                                                             AccessMeansDTO accessMeans,
                                                             RestTemplateManager restTemplateManager,
                                                             String psuIpAddress,
                                                             Instant transactionsFetchStartTime,
                                                             Signer signer) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();
        var providerState = providerStateMapper.fromJson(accessMeans.getAccessMeans(), AtruviaAccessMeans.class);
        var consentId = providerState.authorizedConsentId();
        var baseUrl = properties.getRegionalBankList().stream()
                .filter(regionalBank -> regionalBank.getCode().equals(providerState.selectedRegionalBankCode()))
                .findFirst()
                .orElseThrow(TokenInvalidException::new)
                .getBaseUrl();
        var httpClient = httpClientFactory.createHttpClient(restTemplateManager, providerDisplayName, baseUrl);

        try {
            for (Account account : fetchAccounts(httpClient, consentId, psuIpAddress, authMeans, signer)) {
                String accountId = account.getResourceId();

                List<Balance> balances = fetchBalances(httpClient, consentId, accountId, psuIpAddress, authMeans, signer);
                List<ProviderTransactionDTO> transaction = fetchTransactions(httpClient, consentId, accountId, psuIpAddress,
                        transactionsFetchStartTime, authMeans, signer);

                ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(providerDisplayName, account, balances, transaction);
                providerAccountsDTO.add(providerAccountDTO);

            }

            return new DataProviderResponse(providerAccountsDTO);

        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<Account> fetchAccounts(AtruviaGroupHttpClient httpClient,
                                        String consentId,
                                        String psuIpAddress,
                                        AtruviaGroupAuthenticationMeans authMeans,
                                        Signer signer) throws TokenInvalidException {
        AccountsResponse response = httpClient.getAccounts(consentId, psuIpAddress,
                authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), signer);
        if (Objects.isNull(response) || response.getAccounts() == null) {
            throw new MissingDataException("Empty account's response");
        }
        return response.getAccounts();
    }

    private List<Balance> fetchBalances(AtruviaGroupHttpClient httpClient,
                                        String consentId,
                                        String accountId,
                                        String psuIpAddress,
                                        AtruviaGroupAuthenticationMeans authMeans,
                                        Signer signer) throws TokenInvalidException {
        BalancesResponse response = httpClient.getBalances(accountId, consentId, psuIpAddress,
                authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), signer);
        if (Objects.isNull(response) || response.getBalances() == null) {
            throw new MissingDataException("Empty balance's response");
        }
        return response.getBalances();
    }

    private List<ProviderTransactionDTO> fetchTransactions(AtruviaGroupHttpClient httpClient,
                                                           String consentId,
                                                           String accountId,
                                                           String psuIpAddress,
                                                           Instant transactionsFetchStartTime,
                                                           AtruviaGroupAuthenticationMeans authMeans,
                                                           Signer signer) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();
        String dateFrom = dateConverter.toDateFormat(transactionsFetchStartTime);

        TransactionsResponse response = httpClient.getTransactions(accountId, consentId, psuIpAddress, dateFrom,
                authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), signer);
        transactionsDTO.addAll(processTransactionsResponse(response));

        var url = response.getNextHref();
        var pageCounter = 2;
        while (StringUtils.isNotEmpty(url) && (pageCounter <= properties.getPaginationLimit())) {
            response = httpClient.getTransactions(url, consentId, psuIpAddress,
                    authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), signer);
            transactionsDTO.addAll(processTransactionsResponse(response));

            if (!url.equals(response.getNextHref())) {
                url = response.getNextHref();
            } else {
                url = null;
            }

            pageCounter++;
        }
        return transactionsDTO;
    }

    private List<ProviderTransactionDTO> processTransactionsResponse(TransactionsResponse response) {
        if (Objects.isNull(response)) {
            throw new MissingDataException("Empty transaction's response");
        }
        return new ArrayList<>(transactionMapper.mapProviderTransactionsDTO(response.getBookedTransactions()));
    }
}
