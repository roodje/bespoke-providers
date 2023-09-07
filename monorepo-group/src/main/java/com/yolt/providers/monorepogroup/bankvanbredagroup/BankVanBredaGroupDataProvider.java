package com.yolt.providers.monorepogroup.bankvanbredagroup;

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
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupAccessMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaLoginUrlData;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service.BankVanBredaGroupAuthenticationService;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service.BankVanBredaFetchDataService;
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

import static com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupAuthenticationMeans.*;

@RequiredArgsConstructor
public class BankVanBredaGroupDataProvider implements UrlDataProvider {

    private final String providerIdentifier;
    private final String providerIdentifierDisplayName;
    private final ProviderVersion version;

    private final BankVanBredaGroupAuthenticationService authenticationService;
    private final BankVanBredaFetchDataService fetchDataService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws TokenInvalidException, ProviderFetchDataException {
        BankVanBredaGroupAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlFetchData.getAuthenticationMeans(), providerIdentifier);
        BankVanBredaGroupAccessMeans accessMeans = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        return fetchDataService.fetchData(urlFetchData,
                authenticationMeans,
                accessMeans,
                urlFetchData.getPsuIpAddress());
    }

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        BankVanBredaGroupAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), providerIdentifier);
        BankVanBredaLoginUrlData loginUrlData = authenticationService.getLoginUrlData(
                authenticationMeans,
                urlGetLogin.getRestTemplateManager(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState(),
                urlGetLogin.getPsuIpAddress()
        );
        return new RedirectStep(loginUrlData.getLoginUrl(), loginUrlData.getConsentId(), loginUrlData.getCodeVerifier());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        BankVanBredaGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), providerIdentifier);
        String redirectUrl = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        final String error = queryParams.get("error");
        if (StringUtils.isNotEmpty(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
        }

        final String authorizationCode = queryParams.get("code");
        if (ObjectUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }

        BankVanBredaGroupAccessMeans accessMeans = authenticationService.getUserToken(authMeans,
                urlCreateAccessMeans.getBaseClientRedirectUrl(),
                urlCreateAccessMeans.getProviderState(),
                urlCreateAccessMeans.getRestTemplateManager(),
                authorizationCode);

        String serializedAccessMeans = serializeAccessMeans(accessMeans);

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        serializedAccessMeans,
                        new Date(clock.millis()),
                        Date.from(Instant.ofEpochMilli(accessMeans.getTokens().getExpiryTimestamp()))
                )
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        AccessMeansDTO oldAccessMeans = urlRefreshAccessMeans.getAccessMeans();
        BankVanBredaGroupAccessMeans accessMeans = deserializeAccessMeans(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
        BankVanBredaGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), providerIdentifier);

        BankVanBredaGroupAccessMeans newAccessMeans = authenticationService.refreshUserToken(
                accessMeans.getTokens().getRefreshToken(),
                authMeans,
                urlRefreshAccessMeans.getRestTemplateManager()
        );
        return new AccessMeansDTO(
                oldAccessMeans.getUserId(),
                serializeAccessMeans(newAccessMeans),
                Date.from(Instant.now(clock)),
                Date.from(Instant.ofEpochMilli(accessMeans.getTokens().getExpiryTimestamp()))
        );
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        BankVanBredaGroupAuthenticationMeans authMeans = createAuthenticationMeans(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), providerIdentifier);
        authenticationService.deleteConsent(authMeans, urlOnUserSiteDeleteRequest.getRestTemplateManager(), urlOnUserSiteDeleteRequest.getExternalConsentId());
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
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        typedAuthenticationMeans.put(TPP_ID, TypedAuthenticationMeans.TPP_ID);
        return typedAuthenticationMeans;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME);
    }

    private String serializeAccessMeans(BankVanBredaGroupAccessMeans accessMeans) {
        validateAccessMeans(accessMeans);
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private void validateAccessMeans(BankVanBredaGroupAccessMeans accessMeans) {
        if (accessMeans == null ||
                accessMeans.getConsentId() == null ||
                accessMeans.getTokens() == null ||
                accessMeans.getTokens().getAccessToken() == null ||
                accessMeans.getTokens().getExpiryTimestamp() == null ||
                accessMeans.getTokens().getRefreshToken() == null) {
            throw new GetAccessTokenFailedException("Missing token data");
        }
    }

    private BankVanBredaGroupAccessMeans deserializeAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, BankVanBredaGroupAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize token from access means");
        }
    }
}
