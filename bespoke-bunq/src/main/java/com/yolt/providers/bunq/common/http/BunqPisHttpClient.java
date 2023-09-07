package com.yolt.providers.bunq.common.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.model.*;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class BunqPisHttpClient extends DefaultHttpClient {

    private static final String INSTALLATION = "post_installation";
    private static final String DEVICE_SERVER = "post_device_server";
    private static final String SESSION_SERVER = "post_session_server";

    private final BunqHttpErrorHandler httpErrorHandler;

    public BunqPisHttpClient(MeterRegistry registry, RestTemplate restTemplate, String provider, BunqHttpErrorHandler httpErrorHandler) {
        super(registry, restTemplate, provider);
        this.httpErrorHandler = httpErrorHandler;
    }

    public Psd2SessionResponse createPsd2Session(HttpEntity<SessionServerRequest> createSessionRequest, String createSessionUrl) throws TokenInvalidException {
        return exchange(createSessionUrl, HttpMethod.POST, createSessionRequest, SESSION_SERVER, Psd2SessionResponse.class, httpErrorHandler).getBody();
    }

    public ResponseEntity<JsonNode> createPayment(HttpEntity<PaymentServiceProviderDraftPaymentRequest> httpEntity, String initiateDraftPaymentUrl) throws TokenInvalidException {
        return exchange(initiateDraftPaymentUrl, HttpMethod.POST, httpEntity, ProviderClientEndpoints.INITIATE_PAYMENT, JsonNode.class, httpErrorHandler);
    }

    public ResponseEntity<JsonNode> getPaymentStatus(HttpEntity<Void> httpEntity, String statusDraftPaymentUrl) throws TokenInvalidException {
        return exchange(statusDraftPaymentUrl, HttpMethod.GET, httpEntity, ProviderClientEndpoints.GET_PAYMENT_STATUS, JsonNode.class, httpErrorHandler);
    }

    public InstallationResponse createInstallation(HttpEntity<InstallationRequest> httpEntity, String installationUrl) throws TokenInvalidException {
        return exchange(installationUrl, HttpMethod.POST, httpEntity, INSTALLATION, InstallationResponse.class, httpErrorHandler).getBody();
    }

    public void createDeviceServer(HttpEntity<DeviceServerRequest> httpEntity, String deviceServerUrl) throws TokenInvalidException {
        exchange(deviceServerUrl, HttpMethod.POST, httpEntity, DEVICE_SERVER, DeviceServerResponse.class, httpErrorHandler);
    }

    public void exchangeAuthorizationCodeForAccessToken(String oauthTokenUrl) throws TokenInvalidException {
        exchange(oauthTokenUrl, HttpMethod.POST, HttpEntity.EMPTY, ProviderClientEndpoints.GET_ACCESS_TOKEN, OauthAccessTokenResponse.class, httpErrorHandler).getBody();
    }
}
