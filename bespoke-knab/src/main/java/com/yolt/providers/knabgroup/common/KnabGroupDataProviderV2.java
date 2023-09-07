package com.yolt.providers.knabgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationServiceV2;
import com.yolt.providers.knabgroup.common.data.KnabGroupFetchDataServiceV2;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import com.yolt.providers.knabgroup.common.exception.UnexpectedJsonElementException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans.*;
import static com.yolt.providers.knabgroup.common.configuration.KnabGroupKeyRequirements.KNAB_GROUP_KEY_REQUIREMENTS;

@RequiredArgsConstructor
public class KnabGroupDataProviderV2 implements UrlDataProvider {

    private final KnabGroupAuthenticationServiceV2 authenticationService;
    private final KnabGroupFetchDataServiceV2 fetchDataService;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        KnabGroupAuthenticationMeans authMeans = createKnabGroupAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
        String redirectUrl = urlGetLogin.getBaseClientRedirectUrl();
        String consentId = authenticationService.createConsent(authMeans,
                urlGetLogin.getRestTemplateManager(),
                urlGetLogin.getPsuIpAddress(),
                redirectUrl,
                urlGetLogin.getSigner());
        String loginUrl = authenticationService.getClientLoginUrl(authMeans.getClientId(),
                consentId,
                redirectUrl,
                urlGetLogin.getState());
        return new RedirectStep(loginUrl, consentId, null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        KnabGroupAuthenticationMeans authMeans = createKnabGroupAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        String authorizationCode = extractAuthorizationCode(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite());
        KnabAccessMeans knabAccessMeans = authenticationService.createAccessMeans(authorizationCode,
                urlCreateAccessMeans.getBaseClientRedirectUrl(),
                urlCreateAccessMeans.getRestTemplateManager(),
                authMeans);
        AccessMeansDTO accessMeans = new AccessMeansDTO(
                urlCreateAccessMeans.getUserId(),
                accessMeansToJson(knabAccessMeans),
                new Date(Instant.now(clock).getEpochSecond()),
                new Date(knabAccessMeans.getExpiryTimestamp())
        );
        return new AccessMeansOrStepDTO(accessMeans);
    }

    private String extractAuthorizationCode(final String urlWithAuthenticationCode) {
        Map<String, String> urlParameters = UriComponentsBuilder
                .fromUriString(urlWithAuthenticationCode)
                .build()
                .getQueryParams()
                .toSingleValueMap();
        if (urlParameters.keySet()
                .stream()
                .anyMatch(key -> key.contains("error"))) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + urlWithAuthenticationCode);
        }

        String authenticationCode = urlParameters.get("code");
        if (StringUtils.isEmpty(authenticationCode)) {
            throw new GetAccessTokenFailedException("No authentication code in redirect url posted back from the bank");
        }
        return authenticationCode;
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        KnabGroupAuthenticationMeans authMeans = createKnabGroupAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        AccessMeansDTO genericAccessMeans = urlRefreshAccessMeans.getAccessMeans();
        KnabAccessMeans knabAccessMeans = jsonToAccessMeans(genericAccessMeans.getAccessMeans());
        String refreshToken = knabAccessMeans.getRefreshToken();
        if (StringUtils.isEmpty(refreshToken)) {
            throw new TokenInvalidException("Missing refresh token");
        }
        KnabAccessMeans refreshedAccessMeans = authenticationService.refreshAccessMeans(refreshToken,
                urlRefreshAccessMeans.getRestTemplateManager(),
                authMeans);
        Date currentTime = new Date(Instant.now(clock).toEpochMilli());
        Date expireTime = new Date(refreshedAccessMeans.getExpiryTimestamp());
        String jsonAccessMeans = accessMeansToJson(refreshedAccessMeans);
        UUID userId = genericAccessMeans.getUserId();
        return new AccessMeansDTO(userId, jsonAccessMeans, currentTime, expireTime);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        KnabGroupAuthenticationMeans authMeans = createKnabGroupAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        KnabAccessMeans knabAccessMeans = jsonToAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        return fetchDataService.fetchData(knabAccessMeans,
                authMeans,
                urlFetchData.getRestTemplateManager(),
                urlFetchData.getSigner(),
                urlFetchData.getPsuIpAddress(),
                urlFetchData.getTransactionsFetchStartTime());
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(new KeyRequirements(KNAB_GROUP_KEY_REQUIREMENTS, TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(new KeyRequirements(KNAB_GROUP_KEY_REQUIREMENTS, SIGNING_KEY_ID, SIGNING_CERTIFICATE_NAME));
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentifierDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }


    @Override
    public ConsentValidityRules getConsentValidityRules() {
        Set<String> keywords = new HashSet<>();
        keywords.add("login");
        return new ConsentValidityRules(keywords);
    }

    private String accessMeansToJson(final KnabAccessMeans accessMeans) {
        try {
            return mapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Could not convert access means of " + getProviderIdentifier() + " to JSON");
        }
    }

    private KnabAccessMeans jsonToAccessMeans(final String json) throws TokenInvalidException {
        try {
            return mapper.readValue(json, KnabAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to parse " + getProviderIdentifier() + " access means from JSON");
        }
    }
}
