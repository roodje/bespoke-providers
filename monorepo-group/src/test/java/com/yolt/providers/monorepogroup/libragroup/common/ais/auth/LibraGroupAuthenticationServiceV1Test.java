package com.yolt.providers.monorepogroup.libragroup.common.ais.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.SigningData;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupSampleAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.*;
import com.yolt.providers.monorepogroup.libragroup.common.config.LibraGroupProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.createAuthenticationMeans;
import static com.yolt.providers.monorepogroup.libragroup.common.LibraGroupSampleAuthenticationMeans.CLIENT_ID_SAMPLE;
import static com.yolt.providers.monorepogroup.libragroup.common.LibraGroupSampleAuthenticationMeans.CLIENT_SECRET_SAMPLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraGroupAuthenticationServiceV1Test {

    private static final String REDIRECT_URL = "redirectUrl";
    private static final String AUTH_CODE = "authCode";
    private static final String STATE = "state";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final long EXPIRES_IN = 10000L;
    private static final String PROVIDER = "provider";
    private static final String CONSENT_ID = "consentId";
    private static final String OAUTH_BASE_URL = "https://oauth.com";


    @Mock
    private LibraGroupHttpClientFactory httpClientFactory;
    @Mock
    private RestTemplateManager restTemplateManager;
    @Mock
    private LibraGroupAuthenticationHttpClient httpClient;
    @Mock
    private LibraGroupProperties properties;
    @Mock
    private Signer signer;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private static final LibraGroupAuthenticationMeans AUTHENTICATION_MEANS = createAuthenticationMeans(new LibraGroupSampleAuthenticationMeans().getAuthenticationMeans(), "PROVIDER");
    private static final SigningData SIGNING_DATA = AUTHENTICATION_MEANS.getSigningData();

    private LibraGroupAuthenticationServiceV1 subject;

    @BeforeEach
    public void setUp() {
        subject = new LibraGroupAuthenticationServiceV1(
                properties,
                httpClientFactory,
                PROVIDER,
                CLOCK
        );
        when(httpClientFactory.buildAuthorizationHttpClient(restTemplateManager))
                .thenReturn(httpClient);
    }

    @Test
    void shouldReturnProperRedirectUrlWhenCorrectDataIsProvidedOnGetLoginUrl() {
        //given
        when(httpClient.getClientCredentialsToken(CLIENT_ID_SAMPLE, CLIENT_SECRET_SAMPLE)).thenReturn(ACCESS_TOKEN);
        when(httpClient.getConsent(SIGNING_DATA, ACCESS_TOKEN, signer, PROVIDER))
                .thenReturn(new TestLibraGroupConsent(CONSENT_ID));
        when(properties.getOAuthBaseUrl()).thenReturn(OAUTH_BASE_URL);
        //when
        LibraLoginUrlData loginUrlData =
                subject.getLoginUrlData(AUTHENTICATION_MEANS, restTemplateManager, REDIRECT_URL, STATE, signer);
        //then
        assertThat(loginUrlData).isEqualTo(new LibraLoginUrlData(
                "https://oauth.com?" +
                        "scope=AIS:consentId&" +
                        "response_type=code&" +
                        "redirect_uri=redirectUrl&" +
                        "client_id=clientId&" +
                        "state=state",
                CONSENT_ID));
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnGetUserToken() {
        //given
        when(httpClient.getUserToken(
                CLIENT_ID_SAMPLE,
                CLIENT_SECRET_SAMPLE,
                AUTH_CODE,
                REDIRECT_URL,
                CONSENT_ID,
                PROVIDER))
                .thenReturn(new TestLibraGroupAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN));
        //when
        LibraGroupAccessMeans accessMeans = subject.getUserToken(
                AUTHENTICATION_MEANS,
                REDIRECT_URL,
                CONSENT_ID,
                restTemplateManager,
                AUTH_CODE);
        //then
        assertThat(toJson(accessMeans))
                .isEqualTo(toJson(
                        new LibraGroupAccessMeans(
                                new TestLibraGroupTokens(ACCESS_TOKEN, CLOCK.millis() + EXPIRES_IN * 1000, REFRESH_TOKEN),
                                REDIRECT_URL,
                                CONSENT_ID)));
    }

    @Test
    void shouldReturnProperUserTokensWhenCorrectDataIsProvidedOnRefreshUserToken() throws TokenInvalidException {
        when(httpClient.refreshUserToken(
                CLIENT_ID_SAMPLE,
                CLIENT_SECRET_SAMPLE,
                REFRESH_TOKEN,
                REDIRECT_URL,
                PROVIDER))
                .thenReturn(new TestLibraGroupAuthData(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN));
        //when
        LibraGroupAccessMeans scopedTokens = subject.refreshUserToken(
                AUTHENTICATION_MEANS,
                REFRESH_TOKEN,
                REDIRECT_URL,
                CONSENT_ID,
                restTemplateManager);
        //then
        assertThat(toJson(scopedTokens))
                .isEqualTo(toJson(new LibraGroupAccessMeans(
                        new TestLibraGroupTokens(ACCESS_TOKEN, CLOCK.millis() + EXPIRES_IN * 1000, REFRESH_TOKEN),
                        REDIRECT_URL,
                        CONSENT_ID)));
    }

    @Test
    void shouldDeleteConsentProperlyWhenCorrectDataIsProvidedOnDeleteConsent() throws TokenInvalidException {
        //when
        subject.deleteConsent(
                SIGNING_DATA,
                restTemplateManager,
                CONSENT_ID,
                signer);
        //then
        verify(httpClient).deleteConsent(CONSENT_ID, SIGNING_DATA, signer);
    }

    private String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Problem when writing " + object + " as string");
        }
    }
}