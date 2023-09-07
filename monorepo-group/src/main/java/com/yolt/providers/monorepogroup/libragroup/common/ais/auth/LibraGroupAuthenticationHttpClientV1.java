package com.yolt.providers.monorepogroup.libragroup.common.ais.auth;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.SigningData;
import com.yolt.providers.monorepogroup.libragroup.common.LibraSigningService;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAuthData;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupConsent;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupConsentRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

import static com.yolt.providers.common.constants.OAuth.*;
import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT;

public class LibraGroupAuthenticationHttpClientV1 extends DefaultHttpClientV3 implements LibraGroupAuthenticationHttpClient {

    private static final String CONSENT_ENDPOINT = "/CONSENTS_API/1.0";
    private static final String TOKEN_ENDPOINT = "/token";
    private static final String ERROR_WHILE_RETRIEVING_CONSENT_ID = "Error while retrieving consentId in ";
    private static final String SCOPE_VALUE = "default";
    private static final String EMPTY_BODY_WITH_TOKEN_RESPONSE = "Empty body with token response in ";
    private static final String X_REQUEST_ID = "X-Request-ID";

    private final LibraSigningService signingService;
    private final HttpErrorHandlerV2 errorHandler;
    private final Clock clock;
    private final String providerIdentifier;

    public LibraGroupAuthenticationHttpClientV1(LibraSigningService signingService,
                                                HttpErrorHandlerV2 errorHandler,
                                                MeterRegistry registry,
                                                RestTemplate restTemplate,
                                                Clock clock,
                                                String providerIdentifier) {
        super(registry, restTemplate, providerIdentifier);
        this.signingService = signingService;
        this.errorHandler = errorHandler;
        this.clock = clock;
        this.providerIdentifier = providerIdentifier;
    }

    @Override
    public String getClientCredentialsToken(String clientId,
                                            String clientSecret) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(GRANT_TYPE, CLIENT_CREDENTIALS);
            body.add(SCOPE, SCOPE_VALUE);
            return fetchTokenResponse(new HttpEntity<>(body, headers), CLIENT_CREDENTIALS_GRANT, providerIdentifier).getAccessToken();
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException(EMPTY_BODY_WITH_TOKEN_RESPONSE + providerIdentifier);
        }
    }

    @Override
    public LibraGroupConsent getConsent(SigningData signingData,
                                        String accessToken,
                                        Signer signer,
                                        String providerIdentifier) {
        try {
            LibraGroupConsentRequest payload = new LibraGroupConsentRequest(clock);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.addAll(signingService.getSigningHeaders(payload,
                    signingData.getSigningCertificateSerialNumber(),
                    signingData.getSigningKeyId(),
                    signingData.getSigningCertificateBase64(),
                    signer));
            HttpEntity<LibraGroupConsentRequest> entity = new HttpEntity<>(payload, headers);
            LibraGroupConsent body = exchange(CONSENT_ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                    LibraGroupConsent.class,
                    errorHandler).getBody();
            if (body == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier + " missing body");
            } else if (body.getConsentId() == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier + " missing consentId");
            }
            return body;
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier, e);
        }
    }

    @Override
    public void deleteConsent(String consentId,
                              SigningData signingData,
                              Signer signer) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(signingService.getSigningHeaders(new LinkedMultiValueMap<>(),
                signingData.getSigningCertificateSerialNumber(),
                signingData.getSigningKeyId(),
                signingData.getSigningCertificateBase64(),
                signer));
        exchange(CONSENT_ENDPOINT + "/" + consentId,
                HttpMethod.DELETE,
                new HttpEntity<Void>(headers),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                errorHandler
        );
    }

    @Override
    public LibraGroupAuthData getUserToken(String clientId,
                                           String clientSecret,
                                           String authCode,
                                           String redirectUri,
                                           String consentId,
                                           String providerIdentifier) {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(CLIENT_SECRET, clientSecret);
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(SCOPE, "AIS:" + consentId);
        requestPayload.add(CODE, authCode);
        requestPayload.add(REDIRECT_URI, redirectUri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestPayload, headers);
        try {
            return fetchTokenResponse(httpEntity, ProviderClientEndpoints.GET_ACCESS_TOKEN, providerIdentifier);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(EMPTY_BODY_WITH_TOKEN_RESPONSE + providerIdentifier);
        }
    }

    @Override
    public LibraGroupAuthData refreshUserToken(String clientId,
                                               String clientSecret,
                                               String refreshToken,
                                               String redirectUrl,
                                               String providerIdentifier) throws TokenInvalidException {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(CLIENT_SECRET, clientSecret);
        requestPayload.add(GRANT_TYPE, REFRESH_TOKEN);
        requestPayload.add(REFRESH_TOKEN, refreshToken);
        requestPayload.add(REDIRECT_URI, redirectUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(requestPayload, headers);
        return fetchTokenResponse(httpEntity, ProviderClientEndpoints.REFRESH_TOKEN, providerIdentifier);
    }

    private LibraGroupAuthData fetchTokenResponse(HttpEntity<MultiValueMap<String, String>> tokenRequest,
                                                  String prometheusPath,
                                                  String providerIdentifier) throws TokenInvalidException {
        LibraGroupAuthData authData = exchange(
                TOKEN_ENDPOINT,
                HttpMethod.POST,
                tokenRequest,
                prometheusPath,
                LibraGroupAuthData.class,
                errorHandler
        ).getBody();
        if (authData == null) {
            throw new TokenInvalidException(EMPTY_BODY_WITH_TOKEN_RESPONSE + providerIdentifier);
        }
        return authData;
    }
}
