package com.yolt.providers.unicredit.common.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditDataMapper;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateTransformer;
import com.yolt.providers.unicredit.common.dto.*;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClient;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UniCreditFetchDataServiceV2 implements UniCreditFetchDataService {

    private final UniCreditAuthMeansMapper authMeansMapper;
    private final ProviderStateTransformer<UniCreditAccessMeansDTO> stateTransformer;
    private final UniCreditHttpClientFactory httpClientFactory;
    private final UniCreditBaseProperties properties;
    private final UniCreditDataMapper dataMapper;
    private final ZoneId zoneId;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData, ProviderInfo providerInfo) throws ProviderFetchDataException, TokenInvalidException {
        UniCreditAuthMeans uniCreditAuthMeans = authMeansMapper.fromBasicAuthenticationMeans(urlFetchData.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditAccessMeansDTO uniCreditAccessMeansDTO = stateTransformer.transformToObject(urlFetchData.getAccessMeans().getAccessMeans());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(uniCreditAuthMeans, urlFetchData.getRestTemplateManager(), providerInfo.getDisplayName(), properties.getBaseUrl());
        FetchDataRequest fetchDataRequest = FetchDataRequest.builder()
                .consentId(uniCreditAccessMeansDTO.getConsentId())
                .psuIpAddress(urlFetchData.getPsuIpAddress())
                .fetchStartTime(urlFetchData.getTransactionsFetchStartTime())
                .build();

        return getAccountsData(httpClient, fetchDataRequest);
    }

    private DataProviderResponse getAccountsData(UniCreditHttpClient httpClient, FetchDataRequest fetchDataRequest) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccounts = new ArrayList<>();

        List<UniCreditAccountDTO> supportedAccounts = getAccounts(httpClient, fetchDataRequest)
                .stream()
                .filter(it -> dataMapper.verifyAccountType(it.getCashAccountType()))
                .collect(Collectors.toList());

        String accountId;
        for (UniCreditAccountDTO accountDTO : supportedAccounts) {
            accountId = accountDTO.getResourceId();
            try {
                List<UniCreditBalanceDTO> balanceDTOs = getBalancesForAccount(httpClient, accountId, fetchDataRequest);
                List<UniCreditTransactionsDTO> transactions = getTransactionsForAccount(httpClient, accountId, fetchDataRequest);
                providerAccounts.add(dataMapper.mapToAccount(accountDTO, transactions, balanceDTOs));
            } catch (RuntimeException ex) {
                throw new ProviderFetchDataException(ex);
            }
        }


        return new DataProviderResponse(providerAccounts);
    }

    private List<UniCreditTransactionsDTO> getTransactionsForAccount(final UniCreditHttpClient httpClient, final String accountId, final FetchDataRequest fetchDataRequest) throws TokenInvalidException {
        List<UniCreditTransactionsDTO> transactionsDTOs = new ArrayList<>();
        String url = UriComponentsBuilder.fromUriString(UniCreditHttpClient.TRANSACTIONS_ENDPOINT_TEMPLATE)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", prepareTransactionsDateFrom(fetchDataRequest.getFetchStartTime()))
                .buildAndExpand(accountId)
                .toString();

        int pageCounter = 0;
        while (canFetchNextPage(url, pageCounter)) {
            UniCreditTransactionsDTO transactionsDTO = httpClient.getTransactions(url, fetchDataRequest.getConsentId(), fetchDataRequest.getPsuIpAddress());
            transactionsDTOs.add(transactionsDTO);

            url = transactionsDTO.getNextPageUrl();
            pageCounter++;
        }

        return transactionsDTOs;
    }

    private boolean canFetchNextPage(final String nextPageUrl, final int maxPages) {
        return !StringUtils.isEmpty(nextPageUrl) && maxPages < properties.getPaginationLimit();
    }

    private String prepareTransactionsDateFrom(final Instant fetchStartDate) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zoneId).format(fetchStartDate);
    }

    private List<UniCreditBalanceDTO> getBalancesForAccount(final UniCreditHttpClient httpClient, final String accountId, final FetchDataRequest fetchDataRequest) throws TokenInvalidException {
        String url = UriComponentsBuilder.fromUriString(UniCreditHttpClient.BALANCES_ENDPOINT_TEMPLATE)
                .buildAndExpand(accountId)
                .toUriString();
        UniCreditBalancesDTO balancesDTO = httpClient.getBalances(url, fetchDataRequest.getConsentId(), fetchDataRequest.getPsuIpAddress());
        return balancesDTO.getBalances();
    }

    private List<UniCreditAccountDTO> getAccounts(final UniCreditHttpClient httpClient, final FetchDataRequest fetchDataRequest) throws TokenInvalidException, ProviderFetchDataException {
        try {
            UniCreditAccountsDTO accountsDTO = httpClient.getAccounts(UniCreditHttpClient.ACCOUNTS_ENDPOINT, fetchDataRequest.getConsentId(), fetchDataRequest.getPsuIpAddress());
            return accountsDTO.getAccounts();
        } catch (RuntimeException ex) {
            throw new ProviderFetchDataException(ex);
        }
    }
}
