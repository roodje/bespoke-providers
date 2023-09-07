package com.yolt.providers.kbcgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.kbcgroup.common.dto.ConsentUrlData;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupAccessMeans;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupLoginFormDTO;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupTokenResponse;
import com.yolt.providers.kbcgroup.common.exception.LoginNotFoundException;
import com.yolt.providers.kbcgroup.common.service.KbcGroupAuthenticationService;
import com.yolt.providers.kbcgroup.common.service.KbcGroupFetchDataService;
import com.yolt.providers.kbcgroup.common.util.HsmUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans.CLIENT_TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans.CLIENT_TRANSPORT_KEY_ID_NAME;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class KbcGroupDataProvider implements UrlDataProvider {

    private final KbcGroupAuthenticationService kbcGroupAuthenticationService;
    private final KbcGroupFetchDataService kbcGroupFetchDataService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private static final String IBAN_ID = "Iban";

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        return typedAuthenticationMeans;
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        TextField textField = new TextField(IBAN_ID, "IBAN", 34, 34, false);
        Form form = new Form(Collections.singletonList(textField), null, null);
        try {
            return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(Duration.ofHours(1)),
                    objectMapper.writeValueAsString(new KbcGroupLoginFormDTO(urlGetLogin.getAuthenticationMeansReference(), urlGetLogin.getBaseClientRedirectUrl())));
        } catch (JsonProcessingException e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return StringUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues())
                ? createProperAccessMeans(urlCreateAccessMeans)
                : returnProperLoginUrl(urlCreateAccessMeans);
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        KbcGroupAuthMeans authMeans = KbcGroupAuthMeans.createAuthMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        String refreshToken = deserializeAccessMeans(urlRefreshAccessMeans.getAccessMeans().getAccessMeans())
                .getKbcGroupTokenResponse()
                .getRefreshToken();
        KbcGroupTokenResponse tokenResponse = kbcGroupAuthenticationService.refreshAccessToken(urlRefreshAccessMeans, authMeans, refreshToken);
        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        KbcGroupAccessMeans kbcGroupAccessMeans = deserializeAccessMeans(accessMeansDTO.getAccessMeans());
        return new AccessMeansDTO(
                accessMeansDTO.getUserId(),
                serializeAccessMeans(tokenResponse, kbcGroupAccessMeans.getRedirectUrl(), kbcGroupAccessMeans.getConsentId()),
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()))
        );
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        KbcGroupAuthMeans authMeans = KbcGroupAuthMeans.createAuthMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        KbcGroupAccessMeans accessMeans = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        return kbcGroupFetchDataService.fetchData(urlFetchData,
                authMeans,
                accessMeans);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private AccessMeansOrStepDTO returnProperLoginUrl(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        KbcGroupAuthMeans authMeans = KbcGroupAuthMeans
                .createAuthMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        try {
            KbcGroupLoginFormDTO kbcGroupLoginFormDTO = objectMapper.readValue(urlCreateAccessMeans.getProviderState(), KbcGroupLoginFormDTO.class);
            ConsentUrlData consentUrlData = kbcGroupAuthenticationService.generateLoginUrl(
                    urlCreateAccessMeans.getRestTemplateManager(),
                    authMeans,
                    kbcGroupLoginFormDTO,
                    urlCreateAccessMeans.getFilledInUserSiteFormValues().get(IBAN_ID),
                    urlCreateAccessMeans.getPsuIpAddress(),
                    urlCreateAccessMeans.getState());
            return new AccessMeansOrStepDTO(new RedirectStep(consentUrlData.getConsentUrl(), null, consentUrlData.getCodeVerifier()));
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    private AccessMeansOrStepDTO createProperAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {

        KbcGroupAuthMeans authMeans = KbcGroupAuthMeans
                .createAuthMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        String redirectUrl = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite();

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        final String error = queryParams.get("error");
        if (!StringUtils.isEmpty(error)) {
            // In this case we want to log the redirect URL, because we want to know what went wrong and why.
            // The redirect URL shouldn't contain any sensitive data at this point, because the login was not successful.
            // Also, we return 'TOKEN_INVALID', because of the behavior that the app has for that reason.
            // See the JavaDoc on the enum value for more information.
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
        }

        final String authorizationCode = queryParams.get("code");
        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }

        // Fix for when banks suddenly decide to use a # instead of a ? to designate the start of the query parameters..
        int queryParamStartIndex = redirectUrl.indexOf('?');
        if (queryParamStartIndex == -1) {
            queryParamStartIndex = redirectUrl.indexOf('#');
        }

        redirectUrl = redirectUrl.substring(0, queryParamStartIndex);

        KbcGroupTokenResponse tokenResponse = kbcGroupAuthenticationService.obtainAccessToken(urlCreateAccessMeans,
                authMeans,
                authorizationCode,
                redirectUrl,
                urlCreateAccessMeans.getProviderState());

        String serializedAccessMeans = serializeAccessMeans(tokenResponse, redirectUrl, urlCreateAccessMeans.getProviderState());

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        serializedAccessMeans,
                        new Date(),
                        Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()))
                )
        );
    }

    private String serializeAccessMeans(KbcGroupTokenResponse kbcGroupTokenResponse, String redirectUrl, String consentId) {
        validateAccessToken(kbcGroupTokenResponse);
        try {
            return objectMapper.writeValueAsString(new KbcGroupAccessMeans(kbcGroupTokenResponse, redirectUrl, consentId));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private void validateAccessToken(KbcGroupTokenResponse kbcGroupTokenResponse) {
        if (kbcGroupTokenResponse == null || kbcGroupTokenResponse.getAccessToken() == null
                || kbcGroupTokenResponse.getExpiresIn() == null || kbcGroupTokenResponse.getRefreshToken() == null) {
            throw new GetAccessTokenFailedException("Missing token data");
        }
    }

    private KbcGroupAccessMeans deserializeAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, KbcGroupAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize token from access means");
        }
    }
}
