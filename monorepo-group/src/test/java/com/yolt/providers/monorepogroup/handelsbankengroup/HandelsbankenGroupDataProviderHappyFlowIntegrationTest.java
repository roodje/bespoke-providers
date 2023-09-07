package com.yolt.providers.monorepogroup.handelsbankengroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.TestSigner;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.HandelsbankenGroupAccessMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.HandelsbankenGroupDataProvider;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = HandelsbankenGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/handelsbankengroup/", httpsPort = 0, port = 0)
@ActiveProfiles("handelsbankengroup")
class HandelsbankenGroupDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "a8e767c5-d68f-417f-905f-ffc63a8f052d";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final UUID USER_ID = UUID.fromString("22386389-d130-40ff-b531-62799ebc756d");
    private static final String CONSENT_ID = "a9271b81-3229-4a1f-bf9c-758f11c1f5af";

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("HandelsbankenNlDataProviderV1")
    private HandelsbankenGroupDataProvider dataProvider;

    @Autowired
    @Qualifier("HandelsbankenGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    private static final Signer SIGNER = new TestSigner();

    private static final Map<String, BasicAuthenticationMean> AUTHENTICATION_MEANS = HandelsbankenGroupSampleAuthenticationMeans.postOnboardingAuthMeans();

    @Test
    void shouldOnboardSuccessfully() {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(HandelsbankenGroupSampleAuthenticationMeans.preOnboardingAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(SIGNER)
                .setRedirectUrls(List.of(REDIRECT_URI))
                .setRegistrationOperation(RegistrationOperation.CREATE)
                .build();

        // when
        Map<String, BasicAuthenticationMean> basicAuthenticationMean = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(basicAuthenticationMean.get("tpp-id").getValue()).isEqualTo("SE-FINA-100001");
        assertThat(basicAuthenticationMean.get("client-id").getValue()).isEqualTo("2cffdb50-2323-4be7-a2a2-70a6610f8a06");
    }

    @Test
    void shouldReturnLoginUrl() {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep result = dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(result.getRedirectUrl()).matches(".*/bb/gls5/oauth2/authorize/1.0\\?response_type=code&scope=AIS%253Aa9271b81-3229-4a1f-bf9c-758f11c1f5af&client_id=2cffdb50-2323-4be7-a2a2-70a6610f8a06&state=a8e767c5-d68f-417f-905f-ffc63a8f052d&redirect_uri=https%253A%252F%252Fyolt.com%252Fcallback");
    }

    @Test
    void shouldCreateNewAccessMeans() throws JsonProcessingException {
        // given
        String redirectUrlWithCode = REDIRECT_URI + "?code=02e31fc1-8320-4952-bba9-8ad99fc8094";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrlWithCode)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setProviderState(CONSENT_ID)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        String expectedAccessMeans = objectMapper.writeValueAsString(new HandelsbankenGroupAccessMeans(CONSENT_ID,
                "QVQTI4OWMtZGRlYy00ZDBmLTg3YTktzYxZjk0NGZm",
                "UlQ6NNWMtNWQ3YS00MWUyLTg3NTk5NmUxZDkwNTll",
                1641118656000L)
        );

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(result.getAccessMeans().getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new HandelsbankenGroupAccessMeans(
                        CONSENT_ID,
                        "QVQ6MGQ2YmYtMmZjOC00Yzg3LzYyOGU0NTg5YmM3",
                        "UlQ6NNWMtNWQ3YS00MWUyLTg3NTk5NmUxZDkwNTll",
                        1641118656000L
                )
        );
        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getAccessMeans()).isEqualTo(expectedAccessMeans);
    }


    private AccessMeansDTO getAccessMeans() throws JsonProcessingException {
        HandelsbankenGroupAccessMeans accessMeans = new HandelsbankenGroupAccessMeans(
                CONSENT_ID,
                "QVQTI4OWMtZGRlYy00ZDBmLTg3YTktzYxZjk0NGZm",
                "UlQ6NNWMtNWQ3YS00MWUyLTg3NTk5NmUxZDkwNTll",
                1641118656000L
        );

        return new AccessMeansDTO(USER_ID,
                objectMapper.writeValueAsString(accessMeans),
                new Date(clock.millis()),
                new Date(clock.millis()));
    }
}