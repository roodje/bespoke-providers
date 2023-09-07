package com.yolt.providers.abancagroup.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans;
import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaTokens;
import com.yolt.providers.abancagroup.common.ais.auth.service.AbancaAuthenticationService;
import com.yolt.providers.abancagroup.common.ais.config.HsmUtils;
import com.yolt.providers.abancagroup.common.ais.config.ProviderIdentification;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaFetchDataService;
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
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans.*;

@RequiredArgsConstructor
public class AbancaDataProviderV1 implements UrlDataProvider {

    private final ProviderIdentification providerIdentification;
    private final AbancaAuthenticationService authenticationService;
    private final AbancaFetchDataService fetchDataService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        AbancaAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
        String loginUrl = authenticationService.getLoginUrl(
                authenticationMeans.getClientId(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState()
        );
        return new RedirectStep(loginUrl, null, null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        AbancaAuthenticationMeans authMeans = createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        String redirectUrl = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        final String error = queryParams.get("error");
        if (StringUtils.hasText(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
        }

        final String authorizationCode = queryParams.get("code");
        if (ObjectUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }

        AbancaTokens tokens = authenticationService.getUserToken(authMeans,
                urlCreateAccessMeans.getRestTemplateManager(),
                authorizationCode);

        String serializedAccessMeans = serializeAccessMeans(tokens);

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        serializedAccessMeans,
                        new Date(),
                        Date.from(Instant.ofEpochMilli(tokens.getExpiryTimestamp()))
                )
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        AccessMeansDTO oldAccessMeans = urlRefreshAccessMeans.getAccessMeans();
        AbancaTokens abancaTokens = deserializeAccessMeans(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
        AbancaAuthenticationMeans authMeans = createAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        AbancaTokens newTokens = authenticationService.refreshUserToken(
                abancaTokens.getRefreshToken(),
                authMeans,
                urlRefreshAccessMeans.getRestTemplateManager()
        );
        return new AccessMeansDTO(
                oldAccessMeans.getUserId(),
                serializeAccessMeans(newTokens),
                Date.from(Instant.now(clock)),
                Date.from(Instant.ofEpochMilli(abancaTokens.getExpiryTimestamp()))
        );
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws TokenInvalidException {
        AbancaAuthenticationMeans authMeans = createAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        AbancaTokens abancaTokens = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        return fetchDataService.fetchData(urlFetchData, authMeans, abancaTokens);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_KEY_ID, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_UUID);
        typedAuthenticationMeans.put(API_KEY, TypedAuthenticationMeans.WEB_API_KEY_UUID);
        return typedAuthenticationMeans;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        //Full javascript consent page
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public final String getProviderIdentifier() {
        return providerIdentification.getProviderIdentifier();
    }

    @Override
    public final String getProviderIdentifierDisplayName() {
        return providerIdentification.getProviderDisplayName();
    }

    @Override
    public final ProviderVersion getVersion() {
        return providerIdentification.getProviderVersion();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME);
    }

    private String serializeAccessMeans(AbancaTokens tokens) {
        validateAccessToken(tokens);
        try {
            return objectMapper.writeValueAsString(tokens);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private void validateAccessToken(AbancaTokens tokenResponse) {
        if (tokenResponse == null || tokenResponse.getAccessToken() == null
                || tokenResponse.getExpiryTimestamp() == null || tokenResponse.getRefreshToken() == null) {
            throw new GetAccessTokenFailedException("Missing token data");
        }
    }

    private AbancaTokens deserializeAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, AbancaTokens.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize token from access means");
        }
    }
}
