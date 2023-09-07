package com.yolt.providers.knabgroup.common.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.knabgroup.common.configuration.KnabGroupProperties;
import com.yolt.providers.knabgroup.common.dto.external.AuthData;
import com.yolt.providers.knabgroup.common.dto.external.ConsentResponse;
import com.yolt.providers.knabgroup.common.dto.internal.ConsentRequest;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClient;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.springframework.http.HttpHeaders.DATE;

@RequiredArgsConstructor
@Slf4j
public class KnabGroupAuthenticationServiceV2 {

    private static final int CONSENT_VALIDITY_IN_DAYS = 87;
    private static final String AUTHORIZE_ENDPOINT = "/connect/authorize";

    private static final String PSD2 = "psd2";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    private static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String DIGEST = "Digest";
    public static final List<String> HEADERS_IN_SIGNATURE = List.of(DIGEST, X_REQUEST_ID, DATE, TPP_REDIRECT_URI);
    private static final String SIGNATURE = "Signature";

    private static final String CLIENT_LOGIN_URL_SCOPE_TEMPLATE = "psd2 offline_access AIS:%s";

    private static final String ACCESS_TOKEN_FAILED_MESSAGE = "Empty body with token response";
    private static final String CONSENT_FAILED_MESSAGE = "Empty body with consent response";


    private final KnabGroupHttpClientFactory httpClientFactory;
    private final KnabSigningService signingService;
    private final KnabGroupProperties properties;
    private final Clock clock;

    public String createConsent(final KnabGroupAuthenticationMeans authMeans,
                                final RestTemplateManager restTemplateManager,
                                final String psuIp,
                                final String redirectUrl,
                                final Signer signer) {
        KnabGroupHttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authMeans);
        String clientToken = createClientToken(httpClient, authMeans);
        return createConsent(httpClient, clientToken, psuIp, redirectUrl, signer, authMeans);
    }

    private String createClientToken(final KnabGroupHttpClient httpClient,
                                     final KnabGroupAuthenticationMeans authMeans) {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        requestPayload.add(SCOPE, PSD2);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setCacheControl(CacheControl.noCache());
        headers.setBasicAuth(authMeans.getClientId(), authMeans.getClientSecret());
        HttpEntity<Map<String, String>> requestObject = new HttpEntity(requestPayload, headers);
        AuthData clientCredentialAuthData = httpClient.postForClientToken(requestObject);
        if (ObjectUtils.isEmpty(clientCredentialAuthData)) {
            throw new GetLoginInfoUrlFailedException(ACCESS_TOKEN_FAILED_MESSAGE);
        }
        return clientCredentialAuthData.getAccessToken();
    }

    private String createConsent(final KnabGroupHttpClient httpClient,
                                 final String clientToken,
                                 final String psuIp,
                                 final String redirectUrl,
                                 final Signer signer,
                                 final KnabGroupAuthenticationMeans authMeans) {
        String consentValidUntil = calculateConsentExpirationDate();
        ConsentRequest consentRequest = new ConsentRequest(consentValidUntil);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setDate(Instant.now(clock));
        if (psuIp != null) {
            headers.set(PSU_IP_ADDRESS, psuIp);
        }
        headers.set(TPP_SIGNATURE_CERTIFICATE, authMeans.getSigningCertificateInBase64());
        headers.set(TPP_REDIRECT_URI, redirectUrl);
        headers.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        headers.add(DIGEST, signingService.calculateDigest(consentRequest));
        headers.add(SIGNATURE, signingService.calculateSignature(headers, authMeans.getSigningData(signer), HEADERS_IN_SIGNATURE));
        HttpEntity<ConsentRequest> requestObject = new HttpEntity<>(consentRequest, headers);
        ConsentResponse consentResponse = httpClient.postForConsent(requestObject);
        if (ObjectUtils.isEmpty(consentResponse)) {
            throw new GetLoginInfoUrlFailedException(CONSENT_FAILED_MESSAGE);
        }
        return consentResponse.getConsentId();
    }

    private String calculateConsentExpirationDate() {
        return LocalDate.now(clock)
                .plusDays(CONSENT_VALIDITY_IN_DAYS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getClientLoginUrl(final String clientId,
                                    final String consentId,
                                    final String redirectUri,
                                    final String loginState) {
        final String loginUrlScope = String.format(CLIENT_LOGIN_URL_SCOPE_TEMPLATE, consentId);
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(RESPONSE_TYPE, CODE);
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(REDIRECT_URI, redirectUri);
        requestPayload.add(SCOPE, loginUrlScope);
        requestPayload.add(STATE, loginState);

        return UriComponentsBuilder.fromHttpUrl(properties.getAuthorizationUrl() + AUTHORIZE_ENDPOINT)
                .queryParams(requestPayload)
                .build()
                .toString();
    }

    public KnabAccessMeans createAccessMeans(final String authorizationCode,
                                             final String redirectUri,
                                             final RestTemplateManager restTemplateManager,
                                             final KnabGroupAuthenticationMeans authMeans) {
        KnabGroupHttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authMeans);
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(CODE, authorizationCode);
        requestPayload.add(REDIRECT_URI, redirectUri);
        requestPayload.add(CLIENT_ID, authMeans.getClientId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(authMeans.getClientId(), authMeans.getClientSecret());
        headers.setCacheControl(CacheControl.noCache());
        HttpEntity<Map<String, String>> request = new HttpEntity(requestPayload, headers);
        AuthData authData = httpClient.postForUserTokenWithAuthorizationCode(request);
        if (ObjectUtils.isEmpty(authData)) {
            throw new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE);
        }
        validateConsentIdValue(authData.getScope());
        return new KnabAccessMeans(authData, clock);
    }

    public KnabAccessMeans refreshAccessMeans(final String refreshToken,
                                              final RestTemplateManager restTemplateManager,
                                              final KnabGroupAuthenticationMeans authMeans) throws TokenInvalidException {
        KnabGroupHttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authMeans);
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(CLIENT_ID, authMeans.getClientId());
        requestPayload.add(CLIENT_SECRET, authMeans.getClientSecret());
        requestPayload.add(GRANT_TYPE, REFRESH_TOKEN);
        requestPayload.add(REFRESH_TOKEN, refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> request = new HttpEntity(requestPayload, headers);
        AuthData authData = httpClient.postForUserTokenWithRefreshToken(request);
        return new KnabAccessMeans(authData, clock);
    }

    private void validateConsentIdValue(final String scope) {
        Optional<String> singleScopeWithConsentId = Arrays.stream(scope.split(" ")).filter(value -> value.contains("AIS")).findFirst();
        if (!singleScopeWithConsentId.isPresent()) {
            throw new GetAccessTokenFailedException("Missing consentId in scope of user accessToken");
        }
    }
}