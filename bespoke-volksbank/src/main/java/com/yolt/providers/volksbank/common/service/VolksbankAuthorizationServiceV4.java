package com.yolt.providers.volksbank.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.model.VolksbankAccessMeans;
import com.yolt.providers.volksbank.common.model.VolksbankAccessTokenResponse;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientV4;
import com.yolt.providers.volksbank.dto.v1_1.AccountAccess;
import com.yolt.providers.volksbank.dto.v1_1.InitiateConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

@RequiredArgsConstructor
public class VolksbankAuthorizationServiceV4 {

    private final Clock clock;

    public String getConsentId(final VolksbankAuthenticationMeans authenticationMeans,
                               final VolksbankHttpClientV4 httpClient) throws TokenInvalidException {
        InitiateConsentRequest consentObject = new InitiateConsentRequest();
        consentObject.setCombinedServiceIndicator(false);
        consentObject.setRecurringIndicator(true);
        consentObject.setValidUntil(LocalDate.now(clock).plusDays(90).toString());

        // According to de Volksbank group field frequencyPerDay is mandatory, but currently not used to restrict the number off call
        // so this was set to random value, however in theirs the terms of use is stated
        // that when the number of call will be to much for their systems they will restricted this to a max number of 4
        consentObject.setFrequencyPerDay(10);

        // For de Volksbank access object should contain empty lists
        AccountAccess access = new AccountAccess();
        access.setAccounts(Collections.emptyList());
        access.setTransactions(Collections.emptyList());
        access.setBalances(Collections.emptyList());
        consentObject.setAccess(access);

        return httpClient.generateConsentUrl(authenticationMeans, consentObject).getConsentId();
    }

    public VolksbankAccessTokenResponse createAccessToken(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                          final String redirectUrl,
                                                          final VolksbankAuthenticationMeans authenticationMeans,
                                                          final VolksbankHttpClientV4 httpClient) throws TokenInvalidException {
        final String authorizationCode = UriComponentsBuilder
                .fromUriString(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap().get("code");

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code");
        }

        return httpClient.getAccessToken(authenticationMeans, redirectUrl, authorizationCode);
    }

    public VolksbankAccessTokenResponse createNewAccessTokenFromRefreshToken(final VolksbankAccessMeans accessMeansDTO,
                                                                             final VolksbankAuthenticationMeans authenticationMeans,
                                                                             final VolksbankHttpClientV4 httpClient) throws TokenInvalidException {

        return httpClient.getNewAccessTokenUsingRefreshToken(
                authenticationMeans,
                accessMeansDTO.getRedirectUrl(),
                accessMeansDTO.getResponse().getRefreshToken());
    }
}
