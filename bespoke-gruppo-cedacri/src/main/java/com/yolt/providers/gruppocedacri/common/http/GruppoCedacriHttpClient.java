package com.yolt.providers.gruppocedacri.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentRequest;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentResponse;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.AccountsResponse;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.BalancesResponse;
import com.yolt.providers.gruppocedacri.common.dto.fetchdata.TransactionsResponse;
import com.yolt.providers.gruppocedacri.common.dto.registration.AutoOnboardingRequest;
import com.yolt.providers.gruppocedacri.common.dto.registration.AutoOnboardingResponse;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import org.springframework.util.MultiValueMap;

public interface GruppoCedacriHttpClient {

    AutoOnboardingResponse register(AutoOnboardingRequest autoOnboardingRequest);

    TokenResponse getAccessToken(MultiValueMap<String, String> payload);

    String getAuthorizationUrl(String authorizationToken, String redirectUrl, String psuIpAddress, ConsentRequest consentRequest);

    ConsentResponse createConsent(String authorizationToken, String redirectUrl, String psuIpAddress, ConsentRequest consentRequest);

    void deleteConsent(GruppoCedacriAccessMeans accessMean);

    AccountsResponse getAccounts(GruppoCedacriAccessMeans accessMeans, String psuIpAddress) throws TokenInvalidException;

    BalancesResponse getBalances(GruppoCedacriAccessMeans accessMeans, String psuIpAddress, String accountId) throws TokenInvalidException;

    TransactionsResponse getTransactions(GruppoCedacriAccessMeans accessMeans,
                                         String accountId,
                                         String dateFrom,
                                         String psuIpAddress) throws TokenInvalidException;

    TransactionsResponse getTransactionsNextPage(String nextPageEndpoint,
                                                 GruppoCedacriAccessMeans accessMeans,
                                                 String psuIpAddress) throws TokenInvalidException;

}
