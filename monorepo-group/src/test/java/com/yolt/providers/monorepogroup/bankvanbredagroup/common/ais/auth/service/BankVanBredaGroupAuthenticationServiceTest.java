package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.BankVanBredaGroupSampleAuthenticationMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.*;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.service.BankVanBredaGroupHttpClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;

import static com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupAuthenticationMeans.createAuthenticationMeans;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankVanBredaGroupAuthenticationServiceTest {

    private static final String REDIRECT_URL = "redirectUrl";
    private static final String AUTH_CODE = "authCode";
    private static final String STATE = "state";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final long EXPIRES_IN = 10000L;
    private static final String PROVIDER = "provider";
    private static final String CONSENT_ID = "consentId";
    private static final String SCA_O_AUTH = "scaOAuth";
    private static final String AUTHORIZE_ENDPOINT = "https://www.authorize_endpoint.com";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String SCOPE = "AIS:consentId";
    private static final String PSU_IP = "1.1.1.1";


    @Mock
    private BankVanBredaGroupHttpClientFactory httpClientFactory;
    @Mock
    private Clock clock;
    @Mock
    private RestTemplateManager restTemplateManager;
    @Mock
    private BankVanBredaGroupAuthenticationHttpClient httpClient;

    private ObjectMapper objectMapper;


    private BankVanBredaGroupAuthenticationMeans authenticationMeans;
    private BankVanBredaGroupAuthenticationService subject;

    @BeforeEach
    public void setUp() throws IOException, URISyntaxException {
        objectMapper = new ObjectMapper();
        authenticationMeans = createAuthenticationMeans(new BankVanBredaGroupSampleAuthenticationMeans().getAuthenticationMeans(), "PROVIDER");
        subject = new BankVanBredaGroupAuthenticationService(
                httpClientFactory,
                PROVIDER,
                clock
        );
        when(httpClientFactory.buildAuthorizationHttpClient(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                restTemplateManager)
        )
                .thenReturn(httpClient);
    }

    @Test
    void shouldReturnProperRedirectUrlWhenCorrectDataIsProvidedOnGetLoginUrl() {
        //given
        when(httpClient.getConsent(REDIRECT_URL, PSU_IP, PROVIDER)).thenReturn(new TestBankVanBredaGroupConsent(CONSENT_ID, SCA_O_AUTH));
        when(httpClient.getBankLoginUrl(SCA_O_AUTH, PROVIDER)).thenReturn(AUTHORIZE_ENDPOINT);
        //when
        BankVanBredaLoginUrlData loginUrlData = subject.getLoginUrlData(authenticationMeans, restTemplateManager, REDIRECT_URL, STATE, PSU_IP);
        //then

        assertThat(loginUrlData.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(loginUrlData.getCodeVerifier()).isNotNull();
        assertThat(loginUrlData.getLoginUrl()).matches(
                "https://www.authorize_endpoint.com\\?" +
                        "scope=AIS:consentId&" +
                        "client_id=PSDNL-SBX-1234512345&" +
                        "state=state&" +
                        "redirect_uri=redirectUrl&" +
                        "code_challenge=[a-zA-Z0-9-_=].*&" +
                        "code_challenge_method=S256&" +
                        "response_type=code");
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() {
        //given
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(httpClient.getUserToken(AUTH_CODE, authenticationMeans.getTppId(), CODE_VERIFIER, REDIRECT_URL, PROVIDER))
                .thenReturn(new TestBankVanBredaGroupAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, SCOPE));
        //when
        BankVanBredaGroupAccessMeans scopedTokens = subject.getUserToken(
                authenticationMeans,
                REDIRECT_URL,
                CODE_VERIFIER,
                restTemplateManager,
                AUTH_CODE);
        //then
        assertThat(toJson(scopedTokens))
                .isEqualTo(toJson(
                        new BankVanBredaGroupAccessMeans(
                                new TestBankVanBredaGroupTokens(ACCESS_TOKEN, now.toEpochMilli() + EXPIRES_IN * 1000, REFRESH_TOKEN),
                                CONSENT_ID)));
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        when(httpClient.refreshUserToken(REFRESH_TOKEN, authenticationMeans.getTppId(), PROVIDER))
                .thenReturn(new TestBankVanBredaGroupAuthData(ACCESS_TOKEN, EXPIRES_IN, null, SCOPE));
        //when
        BankVanBredaGroupAccessMeans scopedTokens = subject.refreshUserToken(
                REFRESH_TOKEN,
                authenticationMeans,
                restTemplateManager);
        //then
        assertThat(toJson(scopedTokens))
                .isEqualTo(toJson(new BankVanBredaGroupAccessMeans(
                        new TestBankVanBredaGroupTokens(ACCESS_TOKEN, now.toEpochMilli() + EXPIRES_IN * 1000, REFRESH_TOKEN),
                        CONSENT_ID)));
    }

    @Test
    void shouldDeleteConsentProperlyWhenCorrectDataIsProvidedOnDeleteConsent() throws TokenInvalidException {
        //when
        subject.deleteConsent(
                authenticationMeans,
                restTemplateManager,
                CONSENT_ID);
        //then
        verify(httpClient).deleteConsent(CONSENT_ID);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Problem when writing " + object + " as string");
        }
    }
}