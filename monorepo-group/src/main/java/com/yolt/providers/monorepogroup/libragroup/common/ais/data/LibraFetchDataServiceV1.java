package com.yolt.providers.monorepogroup.libragroup.common.ais.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.SigningData;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAccessMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Account;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Accounts;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Balances;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Transactions;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class LibraFetchDataServiceV1 implements LibraFetchDataService {

    private final LibraGroupHttpClientFactory httpClientFactory;
    private final LibraDataMapper dataMapper;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData,
                                          LibraGroupAccessMeans accessMeans,
                                          SigningData signingData,
                                          Signer signer) throws TokenInvalidException, ProviderFetchDataException {
        try {
            LibraGroupDataHttpClient httpClient = httpClientFactory.buildDataHttpClient(
                    urlFetchData.getRestTemplateManager()
            );

            List<ProviderAccountDTO> accounts = new ArrayList<>();

            String accessToken = accessMeans.getTokens().getAccessToken();
            String consentId = accessMeans.getConsentId();
            Accounts rawAccounts = httpClient.getAccounts(
                    consentId,
                    accessToken,
                    signingData,
                    signer);
            for (Account account : rawAccounts.getAccounts()) {
                String resourceId = account.getResourceId();
                List<ProviderTransactionDTO> transactions = getAccountTransactions(
                        httpClient,
                        resourceId,
                        accessToken,
                        consentId,
                        urlFetchData.getTransactionsFetchStartTime());
                Balances balances = httpClient.getBalances(
                        resourceId,
                        consentId,
                        accessToken);
                accounts.add(dataMapper.mapAccountData(account, balances, transactions));
            }

            return new DataProviderResponse(accounts);

        } catch (RuntimeException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<ProviderTransactionDTO> getAccountTransactions(LibraGroupDataHttpClient httpClient,
                                                                String accountId,
                                                                String accessToken,
                                                                String consentId,
                                                                Instant transactionsFetchStartTime) throws TokenInvalidException {
        Transactions firstPageTransactions = httpClient.getAccountTransactions(
                accountId,
                consentId,
                accessToken,
                transactionsFetchStartTime);
        return dataMapper.mapTransactions(firstPageTransactions);
    }
}
