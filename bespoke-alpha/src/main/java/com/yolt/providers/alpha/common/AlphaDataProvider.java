package com.yolt.providers.alpha.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.alpha.common.auth.TypedAuthenticationMeansProducer;
import com.yolt.providers.alpha.common.auth.dto.AlphaAuthMeans;
import com.yolt.providers.alpha.common.auth.dto.AlphaToken;
import com.yolt.providers.alpha.common.auth.service.AuthenticationService;
import com.yolt.providers.alpha.common.config.AlphaKeyRequirementsProducer;
import com.yolt.providers.alpha.common.http.AlphaHttpClient;
import com.yolt.providers.alpha.common.http.AlphaHttpClientFactory;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.NotImplementedException;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.providers.alpha.common.auth.AlphaTypedAuthenticationMeansProducer.SIGNING_CERTIFICATE_NAME;
import static com.yolt.providers.alpha.common.auth.AlphaTypedAuthenticationMeansProducer.SIGNING_PRIVATE_KEY_ID_NAME;

@RequiredArgsConstructor
public class AlphaDataProvider implements UrlDataProvider {

    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;

    private final TypedAuthenticationMeansProducer typedAuthenticationMeansProducer;
    private final AlphaKeyRequirementsProducer alphaKeyRequirementsProducer;
    private final AlphaHttpClientFactory alphaHttpClientFactory;
    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public final Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return alphaKeyRequirementsProducer.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        AlphaAuthMeans authMeans = typedAuthenticationMeansProducer.createAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), providerIdentifier);
        AlphaHttpClient httpClient = alphaHttpClientFactory.createHttpClient(urlGetLogin.getRestTemplateManager(), providerIdentifier);
        try {
            String loginUrl = authenticationService.getLoginInfo(authMeans, httpClient, urlGetLogin.getBaseClientRedirectUrl(),
                    urlGetLogin.getState(), urlGetLogin.getSigner());
            return new RedirectStep(loginUrl);
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException(e.getMessage());
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        AlphaAuthMeans authMeans = typedAuthenticationMeansProducer.createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), providerIdentifier);
        AlphaHttpClient httpClient = alphaHttpClientFactory.createHttpClient(urlCreateAccessMeans.getRestTemplateManager(), providerIdentifier);
        try {
            AlphaToken newAccessMeans = authenticationService.
                    createNewAccessMeans(authMeans, httpClient,
                            urlCreateAccessMeans.getRedirectUrlPostedBackFromSite(), urlCreateAccessMeans.getBaseClientRedirectUrl());
            return new AccessMeansOrStepDTO(createAccessMeansDTOFromTokenResponse(urlCreateAccessMeans.getUserId(), newAccessMeans));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        AlphaAuthMeans authMeans = typedAuthenticationMeansProducer.createAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), providerIdentifier);
        AlphaHttpClient httpClient = alphaHttpClientFactory.createHttpClient(urlRefreshAccessMeans.getRestTemplateManager(), providerIdentifier);
        AlphaToken alphaToken = toOAuthToken(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
        AlphaToken refreshedAccessMeans = authenticationService.refreshAccessMeans(authMeans, httpClient, alphaToken.getRefreshToken());
        return createAccessMeansDTOFromTokenResponse(urlRefreshAccessMeans.getAccessMeans().getUserId(), refreshedAccessMeans);
    }

    private AccessMeansDTO createAccessMeansDTOFromTokenResponse(final UUID userId,
                                                                 final AlphaToken alphaToken) {
        Date expirationDate = Date.from(Instant.now(clock).plusSeconds(alphaToken.getExpiresIn()));
        try {
            return new AccessMeansDTO(userId, objectMapper.writeValueAsString(alphaToken), new Date(clock.millis()), expirationDate);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Couldn't parse Alpha Token to JSON", e);
        }
    }

    private AlphaToken toOAuthToken(String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, AlphaToken.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Couldn't parse access means to Alpha Token", e);
        }
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        throw new NotImplementedException();
    }
}
