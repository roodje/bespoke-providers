package com.yolt.providers.axabanque.common.auth.http.client;

import com.yolt.providers.axabanque.common.auth.http.headerproducer.AuthorizationRequestHeadersProducer;
import com.yolt.providers.axabanque.common.model.external.AuthorizationResponse;
import com.yolt.providers.axabanque.common.model.external.ConsentResponse;
import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.axabanque.common.model.internal.ConsentDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static com.yolt.providers.axabanque.common.auth.service.DefaultAuthenticationService.*;

public class DefaultAuthorizationHttpClientV2 extends DefaultHttpClientV2 implements AuthorizationHttpClient {
    private static final String CONSENT = "/{version}/consents";
    private static final String CONSENT_DELETE = "/{version}/consents/{consentId}";
    private static final String CONSENT_AUTHORIZATION = "/{version}/consents/{consent-id}/authorisations";
    private static final String TOKEN = "/{version}/token";

    private final String endpointVersion;
    private final AuthorizationRequestHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    public DefaultAuthorizationHttpClientV2(MeterRegistry registry, RestTemplate restTemplate, String provider, String endpointVersion, AuthorizationRequestHeadersProducer headersProducer, HttpErrorHandlerV2 errorHandler) {
        super(registry, restTemplate, provider);
        this.endpointVersion = endpointVersion;
        this.headersProducer = headersProducer;
        this.errorHandler = errorHandler;
    }

    @Override
    public ConsentResponse initiateConsent(String redirectUrl, String psuIpAddress, LocalDate validUntil, String xRequestId) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createConsentCreationHeaders(redirectUrl, psuIpAddress, xRequestId);
        ConsentDTO consentRequestDTO = new ConsentDTO(validUntil, 4);
        return exchange(CONSENT, HttpMethod.POST, new HttpEntity<ConsentDTO>(consentRequestDTO, headers), ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, ConsentResponse.class, errorHandler, endpointVersion).getBody();
    }

    @Override
    public AuthorizationResponse initiateAuthorizationResource(ConsentResponse consentRequestDTO, String xRequestId) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createAuthorizationResourceHeaders(xRequestId);
        return exchange(CONSENT_AUTHORIZATION, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, AuthorizationResponse.class, errorHandler, endpointVersion, consentRequestDTO.getConsentId()).getBody();
    }

    @Override
    public Token createToken(String clientId, String redirectUri, String code, String codeVerifier) throws TokenInvalidException {
        MultiValueMap<String, String> body = getPostTokenBody(clientId, redirectUri, code, codeVerifier, "authorization_code");
        HttpHeaders headers = headersProducer.createTokenHeaders();
        return exchange(TOKEN, HttpMethod.POST, new HttpEntity<>(body, headers), ProviderClientEndpoints.GET_ACCESS_TOKEN, Token.class, errorHandler, endpointVersion).getBody();
    }

    @Override
    public Token refreshToken(String clientId, String redirectUri, String code, String codeVerifier, String refreshToken) throws TokenInvalidException {
        MultiValueMap<String, String> body = getPostTokenBody(clientId, redirectUri, code, codeVerifier, "refresh_token");
        body.add(REFRESH_TOKEN, refreshToken);
        HttpHeaders headers = headersProducer.createTokenHeaders();
        return exchange(TOKEN, HttpMethod.POST, new HttpEntity<>(body, headers), ProviderClientEndpoints.REFRESH_TOKEN, Token.class, errorHandler, endpointVersion).getBody();
    }

    @Override
    public void deleteConsent(String xRequestId, String consentId) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.getDeleteConsentHeaders(xRequestId);
        exchange(CONSENT_DELETE, HttpMethod.DELETE, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCESS_TOKEN, Void.class, errorHandler, endpointVersion, consentId);
    }

    private MultiValueMap<String, String> getPostTokenBody(String clientId, String redirectUri, String code, String codeVerifier, String grantType) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, grantType);
        body.add(CLIENT_ID, clientId);
        body.add(CODE, code);
        body.add(REDIRECT_URI, redirectUri);
        body.add(CODE_VERIFIER, codeVerifier);
        return body;
    }
}
