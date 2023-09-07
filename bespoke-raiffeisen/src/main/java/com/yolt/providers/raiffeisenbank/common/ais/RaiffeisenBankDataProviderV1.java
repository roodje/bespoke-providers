package com.yolt.providers.raiffeisenbank.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.raiffeisenbank.common.ais.auth.RaiffeisenBankAuthenticationMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankAccessMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankTokens;
import com.yolt.providers.raiffeisenbank.common.ais.auth.service.RaiffeisenBankAuthenticationService;
import com.yolt.providers.raiffeisenbank.common.ais.config.HsmUtils;
import com.yolt.providers.raiffeisenbank.common.ais.config.ProviderIdentification;
import com.yolt.providers.raiffeisenbank.common.ais.data.service.RaiffeisenBankFetchDataService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.raiffeisenbank.common.ais.auth.RaiffeisenBankAuthenticationMeans.*;

@RequiredArgsConstructor
public class RaiffeisenBankDataProviderV1 implements UrlDataProvider {

    private static final String IBAN_ID = "Iban";
    private static final String USERNAME = "username";

    private final ProviderIdentification providerIdentification;
    private final RaiffeisenBankAuthenticationService authenticationService;
    private final RaiffeisenBankFetchDataService fetchDataService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public FormStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        TextField ibanTextField = new TextField(IBAN_ID, "IBAN", 34, 34, false, false);
        TextField accountTextField = new TextField(USERNAME, "Username", 34, 100, false, false);
        Form form = new Form(List.of(accountTextField, ibanTextField), null, null);
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(Duration.ofHours(1)), null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return ObjectUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues())
                ? createProperAccessMeans(urlCreateAccessMeans)
                : returnProperLoginUrl(urlCreateAccessMeans);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        AccessMeansDTO oldAccessMeans = urlRefreshAccessMeans.getAccessMeans();
        RaiffeisenBankAccessMeans raiffeisenBankAccessMeans = deserializeAccessMeans(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
        RaiffeisenBankAuthenticationMeans authMeans = createAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        RaiffeisenBankTokens newTokens = authenticationService.refreshUserToken(
                raiffeisenBankAccessMeans.getTokens().getRefreshToken(),
                authMeans,
                urlRefreshAccessMeans.getRestTemplateManager()
        );
        return new AccessMeansDTO(
                oldAccessMeans.getUserId(),
                serializeAccessMeans(newTokens, raiffeisenBankAccessMeans.getConsentId()),
                Date.from(Instant.now(clock)),
                Date.from(Instant.ofEpochMilli(raiffeisenBankAccessMeans.getTokens().getExpiryTimestamp()))
        );
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws TokenInvalidException, ProviderFetchDataException {
        RaiffeisenBankAuthenticationMeans authMeans = createAuthenticationMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        RaiffeisenBankAccessMeans accessMeans = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        return fetchDataService.fetchData(urlFetchData, authMeans, accessMeans);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_KEY_ID, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return typedAuthenticationMeans;
    }

    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        RaiffeisenBankAuthenticationMeans authMeans = createAuthenticationMeans(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), getProviderIdentifier());
        RaiffeisenBankAccessMeans accessMeans = deserializeAccessMeans(urlOnUserSiteDeleteRequest.getAccessMeans().getAccessMeans());
        authenticationService.deleteConsent(urlOnUserSiteDeleteRequest.getExternalConsentId(),
                accessMeans.getTokens().getAccessToken(),
                authMeans,
                urlOnUserSiteDeleteRequest.getRestTemplateManager()
        );
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
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

    private AccessMeansOrStepDTO returnProperLoginUrl(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        try {
            RaiffeisenBankAuthenticationMeans authenticationMeans = createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
            RestTemplateManager restTemplateManager = urlCreateAccessMeans.getRestTemplateManager();
            var consentId = authenticationService.createConsentId(
                            authenticationMeans,
                            urlCreateAccessMeans.getBaseClientRedirectUrl(),
                            urlCreateAccessMeans.getPsuIpAddress(),
                            urlCreateAccessMeans.getFilledInUserSiteFormValues().get(IBAN_ID),
                            urlCreateAccessMeans.getFilledInUserSiteFormValues().get(USERNAME),
                            restTemplateManager)
                    .orElseThrow(TokenInvalidException::new);
            var loginUrl = authenticationService.getLoginUrl(
                    authenticationMeans.getClientId(),
                    consentId,
                    urlCreateAccessMeans.getBaseClientRedirectUrl(),
                    urlCreateAccessMeans.getState()
            );
            return new AccessMeansOrStepDTO(new RedirectStep(loginUrl, consentId, consentId));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private AccessMeansOrStepDTO createProperAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {

        RaiffeisenBankAuthenticationMeans authMeans = createAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
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

        RaiffeisenBankTokens tokens = authenticationService.getUserToken(authMeans,
                urlCreateAccessMeans.getRestTemplateManager(),
                authorizationCode,
                urlCreateAccessMeans.getBaseClientRedirectUrl());

        String serializedAccessMeans = serializeAccessMeans(
                tokens,
                urlCreateAccessMeans.getProviderState()
        );

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        serializedAccessMeans,
                        new Date(),
                        Date.from(Instant.ofEpochMilli(tokens.getExpiryTimestamp()))
                )
        );
    }

    private String serializeAccessMeans(RaiffeisenBankTokens tokens, String consentId) {
        validateAccessToken(tokens);
        try {
            return objectMapper.writeValueAsString(new RaiffeisenBankAccessMeans(tokens, consentId));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private void validateAccessToken(RaiffeisenBankTokens tokenResponse) {
        if (tokenResponse == null || tokenResponse.getAccessToken() == null
                || tokenResponse.getExpiryTimestamp() == null || tokenResponse.getRefreshToken() == null) {
            throw new GetAccessTokenFailedException("Missing token data");
        }
    }

    private RaiffeisenBankAccessMeans deserializeAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, RaiffeisenBankAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize token from access means");
        }
    }
}
