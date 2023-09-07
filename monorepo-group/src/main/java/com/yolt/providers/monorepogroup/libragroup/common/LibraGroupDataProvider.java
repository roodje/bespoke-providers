package com.yolt.providers.monorepogroup.libragroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.LibraGroupAuthenticationService;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAccessMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraLoginUrlData;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.LibraFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.*;

@RequiredArgsConstructor
public class LibraGroupDataProvider implements UrlDataProvider {

    private final LibraGroupAuthenticationService authenticationService;
    private final LibraFetchDataService fetchDataService;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        LibraGroupAccessMeans libraAccessMeans = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        LibraGroupAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlFetchData.getAuthenticationMeans(), providerIdentifier);
        return fetchDataService.fetchData(urlFetchData, libraAccessMeans, authenticationMeans.getSigningData(), urlFetchData.getSigner());
    }

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        LibraGroupAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), providerIdentifier);
        LibraLoginUrlData loginUrlData = authenticationService.getLoginUrlData(
                authenticationMeans,
                urlGetLogin.getRestTemplateManager(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState(),
                urlGetLogin.getSigner()
        );
        return new RedirectStep(loginUrlData.getLoginUrl(), loginUrlData.getConsentId(), loginUrlData.getConsentId());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        LibraGroupAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), providerIdentifier);
        String redirectWithAuthCode = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectWithAuthCode)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        final String error = queryParams.get("error");
        if (StringUtils.isNotEmpty(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectWithAuthCode);
        }

        final String authorizationCode = queryParams.get("code");
        if (ObjectUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }
        LibraGroupAccessMeans accessMeans = authenticationService.getUserToken(authenticationMeans,
                urlCreateAccessMeans.getBaseClientRedirectUrl(),
                urlCreateAccessMeans.getProviderState(),
                urlCreateAccessMeans.getRestTemplateManager(),
                authorizationCode);
        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        serializeAccessMeans(accessMeans),
                        new Date(clock.millis()),
                        Date.from(Instant.ofEpochMilli(accessMeans.getTokens().getExpiryTimestamp()))
                ));
    }


    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        LibraGroupAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), providerIdentifier);
        AccessMeansDTO generalAccessMeans = urlRefreshAccessMeans.getAccessMeans();
        LibraGroupAccessMeans libraAccessMeans = deserializeAccessMeans(generalAccessMeans.getAccessMeans());
        LibraGroupAccessMeans newLibraAccessMeans = authenticationService.refreshUserToken(
                authenticationMeans,
                libraAccessMeans.getTokens().getRefreshToken(),
                libraAccessMeans.getRedirectUrl(),
                libraAccessMeans.getConsentId(),
                urlRefreshAccessMeans.getRestTemplateManager());
        return new AccessMeansDTO(
                generalAccessMeans.getUserId(),
                serializeAccessMeans(newLibraAccessMeans),
                new Date(clock.millis()),
                Date.from(Instant.ofEpochMilli(newLibraAccessMeans.getTokens().getExpiryTimestamp()))
        );
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        LibraGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), providerIdentifier);
        authenticationService.deleteConsent(
                authMeans.getSigningData(),
                urlOnUserSiteDeleteRequest.getRestTemplateManager(),
                urlOnUserSiteDeleteRequest.getExternalConsentId(),
                urlOnUserSiteDeleteRequest.getSigner());
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
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(SIGNING_KEY_ID, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);

        return typedAuthenticationMeansMap;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private String serializeAccessMeans(LibraGroupAccessMeans accessMeans) {
        validateAccessMeans(accessMeans);
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private void validateAccessMeans(LibraGroupAccessMeans accessMeans) {
        if (accessMeans == null ||
                accessMeans.getConsentId() == null ||
                accessMeans.getTokens() == null ||
                accessMeans.getTokens().getAccessToken() == null ||
                accessMeans.getTokens().getExpiryTimestamp() == null ||
                accessMeans.getTokens().getRefreshToken() == null) {
            throw new GetAccessTokenFailedException("Missing token data");
        }
    }

    private LibraGroupAccessMeans deserializeAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, LibraGroupAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize token from access means");
        }
    }
}