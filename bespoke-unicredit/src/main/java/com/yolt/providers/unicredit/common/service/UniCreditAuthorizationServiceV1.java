package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateTransformer;
import com.yolt.providers.unicredit.common.dto.ConsentRequestDTO;
import com.yolt.providers.unicredit.common.dto.ConsentResponseDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClient;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RequiredArgsConstructor
public class UniCreditAuthorizationServiceV1 implements UniCreditAuthorizationService {

    private static final int CONSENT_VALID_DAYS = 90;

    private final UniCreditHttpClientFactory httpClientFactory;
    private final UniCreditBaseProperties properties;
    private final ProviderStateTransformer<UniCreditAccessMeansDTO> stateTransformer;
    private final UniCreditAuthMeansMapper authMeansMapper;
    private final ZoneId timeZoneId;
    private final Clock clock;

    /*
     * Call /consents providing information about consent expiration date and call frequency per day
     * In response consentId (used later in data retrieval) and consentURL to which user is redirected and make received consentId valid by logging in.
     */
    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin, final ProviderInfo providerInfo) throws TokenInvalidException {
        Instant consentExpiration = LocalDate.now(clock).atStartOfDay(timeZoneId).toInstant().plus(CONSENT_VALID_DAYS, ChronoUnit.DAYS);
        UniCreditAuthMeans authMeans = authMeansMapper.fromBasicAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, urlGetLogin.getRestTemplateManager(), providerInfo.getDisplayName(), properties.getBaseUrl());
        ConsentResponseDTO consentResponse = httpClient.generateConsent(
                ConsentRequestDTO.createGlobalConsentRequest(new Date(consentExpiration.toEpochMilli()), properties.getFrequencyPerDay(), true),
                urlGetLogin.getPsuIpAddress(),
                urlGetLogin.getBaseClientRedirectUrl() + "?state=" + urlGetLogin.getState(),
                providerInfo.getIdentifier());
        UniCreditAccessMeansDTO accessMeansDTO = new UniCreditAccessMeansDTO(consentResponse.getConsentId(), Instant.now(clock), consentExpiration);
        String providerState = stateTransformer.transformToString(accessMeansDTO);
        return new RedirectStep(consentResponse.getConsentUrl(), null, providerState);
    }

    /*
     * Previously received consentId is formed in appropriate DTO
     */
    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans, final ProviderInfo providerInfo) {
        UniCreditAccessMeansDTO accessMeans = stateTransformer.transformToObject(urlCreateAccessMeans.getProviderState());
        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        urlCreateAccessMeans.getProviderState(),
                        new Date(accessMeans.getCreated().toEpochMilli()),
                        new Date(accessMeans.getExpireTime().toEpochMilli())
                )
        );
    }

    /*
     * There is no refresh supported hence if consentId expires we need to reconsent
     */
    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans, final ProviderInfo providerInfo) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh access means is not supported by UniCredit");
    }
}
