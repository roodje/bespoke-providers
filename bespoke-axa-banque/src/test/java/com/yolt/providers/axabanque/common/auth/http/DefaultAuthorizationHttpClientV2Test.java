package com.yolt.providers.axabanque.common.auth.http;

import com.yolt.providers.axabanque.common.auth.http.client.DefaultAuthorizationHttpClientV2;
import com.yolt.providers.axabanque.common.auth.http.headerproducer.DefaultAuthorizationRequestHeadersProducer;
import com.yolt.providers.axabanque.common.model.external.AuthorizationResponse;
import com.yolt.providers.axabanque.common.model.external.ConsentResponse;
import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.axabanque.common.model.internal.ConsentDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationHttpClientV2Test {

    private MeterRegistry registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    @Mock
    private DefaultAuthorizationRequestHeadersProducer headersProducer;
    @Mock
    private RestTemplate restTemplate;

    private DefaultAuthorizationHttpClientV2 client;

    @BeforeEach
    public void setup() {
        client = new DefaultAuthorizationHttpClientV2(registry, restTemplate, "AXA_BE", "v1", headersProducer, new DefaultHttpErrorHandlerV2());
    }

    @Test
    void shouldSendInitiateConsentRequest() throws TokenInvalidException {
        //given
        HttpHeaders headers = HttpHeaders.EMPTY;
        String redirectUri = "redirectUri";
        String psuIp = "psuIp";
        LocalDate localDate = LocalDate.now();
        ConsentResponse consentResponse = mock(ConsentResponse.class);
        when(headersProducer.createConsentCreationHeaders(redirectUri, psuIp, "xRequestId"))
                .thenReturn(headers);
        when(restTemplate.exchange("/{version}/consents", HttpMethod.POST, new HttpEntity<>(new ConsentDTO(localDate, 4), headers), ConsentResponse.class, "v1"))
                .thenReturn(new ResponseEntity<ConsentResponse>(consentResponse, HttpStatus.OK));
        //when
        ConsentResponse response = client.initiateConsent(redirectUri, psuIp, localDate, "xRequestId");
        //then
        assertThat(response).isEqualTo(consentResponse);
    }


    @Test
    void shouldSendInitiateAuthorizationRequest() throws TokenInvalidException {
        //given
        HttpHeaders headers = HttpHeaders.EMPTY;
        ConsentResponse consentResponse = mock(ConsentResponse.class);
        AuthorizationResponse authorizationResponseDTO = mock(AuthorizationResponse.class);
        when(consentResponse.getConsentId()).thenReturn("consentId");
        when(headersProducer.createAuthorizationResourceHeaders("xRequestTraceId"))
                .thenReturn(headers);
        when(restTemplate.exchange("/{version}/consents/{consent-id}/authorisations", HttpMethod.GET,
                new HttpEntity<>(headers), AuthorizationResponse.class, "v1", consentResponse.getConsentId()))
                .thenReturn(new ResponseEntity<>(authorizationResponseDTO, HttpStatus.OK));
        //when
        AuthorizationResponse response = client.initiateAuthorizationResource(consentResponse, "xRequestTraceId");
        //then
        assertThat(response).isEqualTo(authorizationResponseDTO);
    }

    @Test
    void shouldSendCreateTokenRequest() throws TokenInvalidException {
        //given
        HttpHeaders headers = new HttpHeaders();
        Token tokenDto = mock(Token.class);
        when(headersProducer.createTokenHeaders())
                .thenReturn(headers);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>() {{
            add("client_id", "clientId");
            add("code", "code");
            add("code_verifier", "codeVerifier");
            add("grant_type", "authorization_code");
            add("redirect_uri", "redirectUri");
        }};
        when(restTemplate.exchange("/{version}/token", HttpMethod.POST, new HttpEntity<>(body, headers), Token.class, "v1"))
                .thenReturn(new ResponseEntity<>(tokenDto, HttpStatus.OK));
        //when
        Token response = client.createToken("clientId", "redirectUri", "code", "codeVerifier");
        //then
        assertThat(response).isEqualTo(tokenDto);
    }

    @Test
    void shouldSendRefreshTokenRequest() throws TokenInvalidException {
        //given
        HttpHeaders headers = new HttpHeaders();
        Token tokenDto = mock(Token.class);
        when(headersProducer.createTokenHeaders())
                .thenReturn(headers);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>() {{
            add("client_id", "clientId");
            add("code", "code");
            add("code_verifier", "codeVerifier");
            add("grant_type", "refresh_token");
            add("redirect_uri", "redirectUri");
            add("refresh_token", "refreshToken");
        }};
        when(restTemplate.exchange("/{version}/token", HttpMethod.POST, new HttpEntity<>(body, headers), Token.class, "v1"))
                .thenReturn(new ResponseEntity<>(tokenDto, HttpStatus.OK));
        //when
        Token response = client.refreshToken("clientId", "redirectUri", "code", "codeVerifier", "refreshToken");
        //then
        assertThat(response).isEqualTo(tokenDto);
    }

    @Test
    void shouldDeleteConsent() {
        //given
        HttpHeaders headers = new HttpHeaders();
        when(headersProducer.getDeleteConsentHeaders("xRequestId")).thenReturn(headers);
        when(restTemplate.exchange("/{version}/consents/{consentId}", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, "v1", "consentId"))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> client.deleteConsent("xRequestId", "consentId");

        //then
        assertThatCode(handleMethod).doesNotThrowAnyException();
    }
}
