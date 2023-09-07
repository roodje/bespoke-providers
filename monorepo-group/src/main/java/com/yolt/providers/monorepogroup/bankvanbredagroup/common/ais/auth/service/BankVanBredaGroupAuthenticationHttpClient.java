package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupAuthData;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupAuthorizationEndpoint;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupConsent;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupConsentRequest;
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

public class BankVanBredaGroupAuthenticationHttpClient extends DefaultHttpClientV2 {

    private static final String CONSENT_ENDPOINT = "/berlingroup/v1/consents";
    private static final String TOKEN_ENDPOINT = "/berlingroup/v1/token";
    private static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String ERROR_WHILE_RETRIEVING_CONSENT_ID = "Error while retrieving consentId in ";
    private static final String ERROR_WHILE_RETRIEVING_AUTHORIZATION_ENDPOINT = "Error while retrieving authorization endpoint in ";

    private final HttpErrorHandlerV2 errorHandler;
    private final Clock clock;

    public BankVanBredaGroupAuthenticationHttpClient(HttpErrorHandlerV2 errorHandler,
                                                     MeterRegistry registry,
                                                     RestTemplate restTemplate,
                                                     Clock clock,
                                                     String provider) {
        super(registry, restTemplate, provider);
        this.errorHandler = errorHandler;
        this.clock = clock;
    }

    public BankVanBredaGroupConsent getConsent(String redirectUri,
                                               String psuIp,
                                               String providerIdentifier) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(TPP_REDIRECT_URI, redirectUri);
            headers.add(PSU_IP_ADDRESS, psuIp);
            HttpEntity<BankVanBredaGroupConsentRequest> entity = new HttpEntity<>(new BankVanBredaGroupConsentRequest(clock), headers);
            BankVanBredaGroupConsent body = exchange(CONSENT_ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                    BankVanBredaGroupConsent.class,
                    errorHandler).getBody();
            if (body == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier + " missing body");
            } else if (body.getConsentId() == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier + " missing consentId");
            } else if (body.scaOAuth() == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier + " missing scaOAuth");
            }
            return body;
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_CONSENT_ID + providerIdentifier, e);
        }
    }

    public void deleteConsent(String consentId) throws TokenInvalidException {
        exchange(CONSENT_ENDPOINT + "/" + consentId,
                HttpMethod.DELETE,
                null,
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                errorHandler
        );
    }

    public String getBankLoginUrl(String scaOauth, String providerIdentifier) {
        try {
            BankVanBredaGroupAuthorizationEndpoint body = exchange(
                    scaOauth,
                    HttpMethod.GET,
                    null,
                    ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                    BankVanBredaGroupAuthorizationEndpoint.class,
                    errorHandler).getBody();
            if (body == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_AUTHORIZATION_ENDPOINT + providerIdentifier + " body is empty");
            } else if (body.getAuthorizationUrl() == null) {
                throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_AUTHORIZATION_ENDPOINT + providerIdentifier + " authorizationUrl is empty");
            }
            return body.getAuthorizationUrl();
        } catch (TokenInvalidException e) {
            throw new GetLoginInfoUrlFailedException(ERROR_WHILE_RETRIEVING_AUTHORIZATION_ENDPOINT + providerIdentifier, e);
        }
    }

    public BankVanBredaGroupAuthData getUserToken(String authCode,
                                                  String tppId,
                                                  String codeVerifier,
                                                  String redirectUri,
                                                  String providerIdentifier) {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(CODE, authCode);
        requestPayload.add(CLIENT_ID, tppId);
        requestPayload.add("code_verifier", codeVerifier);
        requestPayload.add(REDIRECT_URI, redirectUri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestPayload, headers);
        try {
            return fetchTokenResponse(httpEntity, ProviderClientEndpoints.GET_ACCESS_TOKEN, providerIdentifier);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Empty body with token response in " + providerIdentifier);
        }
    }

    public BankVanBredaGroupAuthData refreshUserToken(String refreshToken,
                                                      String tppId,
                                                      String providerIdentifier) throws TokenInvalidException {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, REFRESH_TOKEN);
        requestPayload.add(REFRESH_TOKEN, refreshToken);
        requestPayload.add(CLIENT_ID, tppId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestPayload, headers);
        return fetchTokenResponse(httpEntity, ProviderClientEndpoints.REFRESH_TOKEN, providerIdentifier);
    }

    private BankVanBredaGroupAuthData fetchTokenResponse(HttpEntity<MultiValueMap<String, Object>> tokenRequest,
                                                         String prometheusPath,
                                                         String providerIdentifier) throws TokenInvalidException {
        BankVanBredaGroupAuthData authData = exchange(
                TOKEN_ENDPOINT,
                HttpMethod.POST,
                tokenRequest,
                prometheusPath,
                BankVanBredaGroupAuthData.class,
                errorHandler
        ).getBody();
        if (authData == null) {
            throw new TokenInvalidException("Empty body with token response in " + providerIdentifier);
        }
        return authData;
    }
}
