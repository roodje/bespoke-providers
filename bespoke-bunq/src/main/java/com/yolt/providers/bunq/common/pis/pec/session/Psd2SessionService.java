package com.yolt.providers.bunq.common.pis.pec.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import com.yolt.providers.bunq.common.model.*;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.security.KeyPair;
import java.util.Arrays;

@RequiredArgsConstructor
public class Psd2SessionService {

    private static final String PUBLIC_KEY_FORMAT = "-----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----\n";

    private final BunqHttpHeaderProducer httpHeaderProducer;
    private final DefaultEndpointUrlProvider urlProvider;
    private final BunqProperties properties;

    public Psd2SessionResponse createSession(BunqPisHttpClient httpClient, KeyPair keyPair, String psd2apiKey) throws JsonProcessingException, TokenInvalidException {
        var installationResponse = createInstallation(httpClient, keyPair);
        createDeviceServer(httpClient, keyPair, installationResponse.getToken().getTokenString(), psd2apiKey);
        return createSessionAtBunq(httpClient, keyPair, installationResponse.getToken().getTokenString(), psd2apiKey);
    }

    private InstallationResponse createInstallation(BunqPisHttpClient httpClient, KeyPair keyPair) throws TokenInvalidException {
        byte[] encodedPublicKey = keyPair.getPublic().getEncoded();
        String formattedPublicKey = String.format(PUBLIC_KEY_FORMAT, new String(Base64.encode(encodedPublicKey)));
        var installationRequest = new InstallationRequest(formattedPublicKey);
        HttpHeaders headers = httpHeaderProducer.getMandatoryHttpHeaders();
        return httpClient.createInstallation(new HttpEntity<>(installationRequest, headers), urlProvider.getInstallationUrl());
    }

    private void createDeviceServer(BunqPisHttpClient httpClient, KeyPair keyPair, String installationToken, String psd2apiKey) throws JsonProcessingException, TokenInvalidException {
        DeviceServerRequest deviceServerRequest = new DeviceServerRequest("description", psd2apiKey, Arrays.asList(properties.getOurExternalIpAddress(), "*"));
        String deviceServerUrl = urlProvider.getDeviceServerUrl();
        HttpHeaders httpHeaders = httpHeaderProducer.getSignedHeaders(keyPair, installationToken, deviceServerRequest, deviceServerUrl);
        httpClient.createDeviceServer(new HttpEntity<>(deviceServerRequest, httpHeaders), deviceServerUrl);
    }

    private Psd2SessionResponse createSessionAtBunq(BunqPisHttpClient httpClient, KeyPair keyPair, String installationToken, String psd2apiKey) throws JsonProcessingException, TokenInvalidException {
        SessionServerRequest sessionServerRequest = new SessionServerRequest(psd2apiKey);
        String sessionRequestUrl = urlProvider.getSessionServerUrl();
        HttpHeaders httpHeaders = httpHeaderProducer.getSignedHeaders(keyPair, installationToken, sessionServerRequest, sessionRequestUrl);
        return httpClient.createPsd2Session(new HttpEntity<>(sessionServerRequest, httpHeaders), sessionRequestUrl);
    }
}
