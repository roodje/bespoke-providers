package com.yolt.providers.sparkassenandlandesbanks.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAccessMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAuthMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.AccountsResponse;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions.Document;
import com.yolt.providers.sparkassenandlandesbanks.common.mapper.SparkassenAndLandesbanksAccountMapper;
import com.yolt.providers.sparkassenandlandesbanks.common.mapper.SparkassenAndLandesbanksTransactionMapper;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksHttpClient;
import com.yolt.providers.sparkassenandlandesbanks.common.rest.SparkassenAndLandesbanksRestTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SparkassenAndLandesbanksFetchDataService {

    private final SparkassenAndLandesbanksRestTemplateService restTemplateService;
    private final SparkassenAndLandesbanksAccountMapper accountMapper;

    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                          SparkassenAndLandesbanksAuthMeans authMeans,
                                          SparkassenAndLandesbanksAccessMeans accessMeans,
                                          String provider) throws TokenInvalidException, ProviderFetchDataException {
        SparkassenAndLandesbanksHttpClient httpClient = restTemplateService.createHttpClient(authMeans, urlFetchData.getRestTemplateManager(), provider);

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        String psuIpAddress = urlFetchData.getPsuIpAddress();
        try {
            AccountsResponse accountsResponse = httpClient.getAccounts(
                    accessMeans.getDepartment(),
                    accessMeans.getAccessToken(),
                    accessMeans.getConsentId(),
                    psuIpAddress
            );

            for (AccountsResponse.Account account : accountsResponse.getAccounts()) {
                List<ProviderTransactionDTO> transactions = getAccountTransactions(httpClient,
                        accessMeans,
                        account.getResourceId(),
                        urlFetchData.getTransactionsFetchStartTime(),
                        psuIpAddress);
                accounts.add(accountMapper.toProviderAccountDTO(account, account.getBalances(), transactions));
            }
        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }


        return new DataProviderResponse(accounts);
    }

    private List<ProviderTransactionDTO> getAccountTransactions(SparkassenAndLandesbanksHttpClient httpClient,
                                                                SparkassenAndLandesbanksAccessMeans accessMeans,
                                                                String resourceId,
                                                                Instant transactionsFetchStartTime,
                                                                String psuIpAddress) throws TokenInvalidException {
        Document sparkassenAccountTransactionsResponse = httpClient.getAccountTransactions(accessMeans.getDepartment(),
                accessMeans.getAccessToken(),
                accessMeans.getConsentId(),
                resourceId,
                transactionsFetchStartTime,
                psuIpAddress);

        return mapTransactions(sparkassenAccountTransactionsResponse);
    }

    private List<ProviderTransactionDTO> mapTransactions(Document document) {

        if (document == null || document.getBankToCustomerAccountReport() == null ||
                document.getBankToCustomerAccountReport().getReport() == null ||
                document.getBankToCustomerAccountReport().getReport().getTransactionEntries() == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(document.getBankToCustomerAccountReport().getReport().getTransactionEntries())
                .map(SparkassenAndLandesbanksTransactionMapper::toProviderTransactionDto)
                .collect(Collectors.toList());
    }
}