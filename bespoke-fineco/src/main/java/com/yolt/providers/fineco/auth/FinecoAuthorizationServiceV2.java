package com.yolt.providers.fineco.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.fineco.config.FinecoProperties;
import com.yolt.providers.fineco.rest.FinecoHttpClientV2;
import com.yolt.providers.fineco.rest.FinecoRestTemplateServiceV2;
import com.yolt.providers.fineco.v2.dto.AccountAccess;
import com.yolt.providers.fineco.v2.dto.Consents;
import com.yolt.providers.fineco.v2.dto.ConsentsResponse201;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class FinecoAuthorizationServiceV2 {

    private final FinecoRestTemplateServiceV2 restTemplateServiceV2;
    private final FinecoProperties properties;
    private final Clock clock;

    public ConsentsResponse201 getConsentIdWithRedirectUrl(final RestTemplateManager restTemplateManager,
                                                           final FinecoAuthenticationMeans authenticationMeans,
                                                           final String tppRedirectUri,
                                                           final String psuIpAddress) {
        Consents consentObject = new Consents();
        consentObject.setValidUntil(LocalDate.now(clock).plusDays(90));
        consentObject.setCombinedServiceIndicator(false);
        consentObject.setRecurringIndicator(true);
        // On base of fineco answer, max allowed value is 4
        consentObject.setFrequencyPerDay(4);

        // For Fineco bank access object may contain empty lists
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setAccounts(Collections.emptyList());
        accountAccess.setBalances(Collections.emptyList());
        accountAccess.setTransactions(Collections.emptyList());

        consentObject.setAccess(accountAccess);

        FinecoHttpClientV2 httpClientV2 = restTemplateServiceV2.createHttpClient(authenticationMeans, restTemplateManager);
        return httpClientV2.generateConsentUrl(consentObject, tppRedirectUri, authenticationMeans.getClientId(), properties.getConsentUrl(), psuIpAddress);
    }
}