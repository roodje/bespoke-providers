package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.ProviderStateProcessingException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.RaiffeisenAtGroupProviderStateMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupFetchDataService implements RaiffeisenAtGroupFetchDataService {

    private final RaiffeisenAtGroupProviderStateMapper providerStateMapper;

    private final RaiffeisenAtGroupTokenService tokenService;

    private final int paginationLimit;

    @Override
    public FetchDataResult fetchData(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String providerState, final String psuIpAddress, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException {
        try {
            FetchDataResult result = new FetchDataResult();
            var consentId = providerStateMapper.deserialize(providerState).getConsentId();
            var clientAccessToken = tokenService.createClientCredentialToken(httpClient, authenticationMeans);
            var accountsResponse = httpClient.fetchAccounts(clientAccessToken, consentId, psuIpAddress);
            for (Account account : accountsResponse.getAccounts()) {
                var transactionResponse = httpClient.fetchTransaction(account.getResourceId(), clientAccessToken, consentId, psuIpAddress, transactionsFetchStartTime);
                result.addResources(account, transactionResponse.getBookedTransactions(), transactionResponse.getPendingTransactions());
                int fetchedPages = 1;
                String nextPageUrl = transactionResponse.getNextHref();
                while (StringUtils.isNotEmpty(nextPageUrl) && fetchedPages < paginationLimit) {
                    var nextTransactionResponse = httpClient.fetchTransaction(nextPageUrl, clientAccessToken, consentId, psuIpAddress);
                    result.addResources(account, nextTransactionResponse.getBookedTransactions(), nextTransactionResponse.getPendingTransactions());
                    fetchedPages++;
                    if (!nextPageUrl.equals(nextTransactionResponse.getNextHref())) {
                        nextPageUrl = nextTransactionResponse.getNextHref();
                    } else {
                        nextPageUrl = null;
                    }
                }
            }
            return result;
        } catch (ProviderStateProcessingException e) {
            throw new ProviderFetchDataException(e);
        }

    }
}
