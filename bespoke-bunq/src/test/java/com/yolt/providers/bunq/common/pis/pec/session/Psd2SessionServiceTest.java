package com.yolt.providers.bunq.common.pis.pec.session;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import com.yolt.providers.bunq.common.model.*;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.security.KeyPair;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Psd2SessionServiceTest {

    @Mock
    BunqPisHttpClient httpClient;
    @Mock
    BunqHttpHeaderProducer httpHeaderProducer;
    @Mock
    DefaultEndpointUrlProvider urlProvider;
    @Mock
    BunqProperties properties;

    private Psd2SessionService psd2SessionService;

    @BeforeEach
    void setUp() {
        psd2SessionService = new Psd2SessionService(httpHeaderProducer, urlProvider, properties);
    }

    @Test
    void shouldCreateNewSession() throws JsonProcessingException, TokenInvalidException {
        //given
        var authMeans = AuthMeans.prepareAuthMeansV2();
        var expectedBunqAuthMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(authMeans, "BUNQ");
        when(urlProvider.getInstallationUrl()).thenReturn("https://installation.com");
        var mandatoryHeaders = new HttpHeaders();
        when(httpHeaderProducer.getMandatoryHttpHeaders()).thenReturn(mandatoryHeaders);
        var installationToken = mock(Token.class);
        when(installationToken.getTokenString()).thenReturn("installationToken");
        var installationResponse = mock(InstallationResponse.class);
        when(installationResponse.getToken()).thenReturn(installationToken);
        when(httpClient.createInstallation(any(HttpEntity.class), eq("https://installation.com"))).thenReturn(installationResponse);
        when(urlProvider.getDeviceServerUrl()).thenReturn("https://deviceserver.com");
        when(properties.getOurExternalIpAddress()).thenReturn("127.0.0.1");
        var deviceServerRequest = new DeviceServerRequest("description", "6fa459de8951067c68605df354172298df0c1f06b4737aa7ac5531b1de9fe2eb", Arrays.asList("127.0.0.1", "*"));
        var signedHttpHeadersForDeviceServer = new HttpHeaders();
        when(httpHeaderProducer.getSignedHeaders(any(KeyPair.class), eq("installationToken"), eq(deviceServerRequest), eq("https://deviceserver.com")))
                .thenReturn(signedHttpHeadersForDeviceServer);
        doNothing().when(httpClient).createDeviceServer(new HttpEntity<>(deviceServerRequest, signedHttpHeadersForDeviceServer), "https://deviceserver.com");
        when(urlProvider.getSessionServerUrl()).thenReturn("https://sessionserver.com");
        var sessionServerRequest = new SessionServerRequest("6fa459de8951067c68605df354172298df0c1f06b4737aa7ac5531b1de9fe2eb");
        var signedTokenForSessionServerRequest = new HttpHeaders();
        when(httpHeaderProducer.getSignedHeaders(any(KeyPair.class), eq("installationToken"), eq(sessionServerRequest), eq("https://sessionserver.com")))
                .thenReturn(signedTokenForSessionServerRequest);
        var sessionServerResponse = mock(Psd2SessionResponse.class);
        when(httpClient.createPsd2Session(new HttpEntity<>(sessionServerRequest, signedTokenForSessionServerRequest), "https://sessionserver.com")).thenReturn(sessionServerResponse);
        var keyPair = SecurityUtils.generateKeyPair();

        //when
        Psd2SessionResponse result = psd2SessionService.createSession(
                httpClient,
                keyPair,
                expectedBunqAuthMeans.getPsd2apiKey());

        //then
        assertThat(result).isEqualTo(sessionServerResponse);
    }
}