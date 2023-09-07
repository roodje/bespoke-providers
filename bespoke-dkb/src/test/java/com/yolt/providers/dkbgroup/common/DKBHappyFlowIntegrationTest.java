package com.yolt.providers.dkbgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.dkbgroup.DKBSampleAuthenticationMeans;
import com.yolt.providers.dkbgroup.SignerMock;
import com.yolt.providers.dkbgroup.TestApp;
import com.yolt.providers.dkbgroup.common.model.DKBAccessMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.PasswordField;
import nl.ing.lovebird.providershared.form.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dkb")
@AutoConfigureWireMock(stubs = "classpath:/stubs/happyflow", port = 0, httpsPort = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DKBHappyFlowIntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private static RestTemplateManager restTemplateManagerMock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new DKBSampleAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new SimpleRestTemplateManagerMock();
    }

    @Autowired
    @Qualifier("DKBDataProviderV1")
    private DKBGroupProvider dkbDataProviderV1;

    private Stream<DKBGroupProvider> getProviders() {
        return Stream.of(dkbDataProviderV1);
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnFormStepWithTextAndPasswordField(UrlDataProvider dataProvider) {
        //given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier")
                .setState(UUID.randomUUID().toString())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();
        // when
        FormStep formStep = (FormStep) dataProvider.getLoginInfo(urlGetLoginRequest);
        // then
        Form form = formStep.getForm();
        TextField username = (TextField) form.getFormComponents().get(0);
        PasswordField password = (PasswordField) form.getFormComponents().get(1);
        assertThat(form.getFormComponents().size()).isEqualTo(2);
        assertThat(formStep.getTimeoutTime()).isCloseTo(Instant.now().plusSeconds(3600), byLessThan(1, ChronoUnit.MINUTES));
        assertThat(form.getExplanationField().getExplanation()).isEqualTo("Please enter your login and password for DKB.");
        assertThat(form.getExplanationField().getDisplayName()).isEqualTo("explanation");
        assertThat(form.getExplanationField().getId()).isEqualTo("explanation");
        assertThat(username.getDisplayName()).isEqualTo("username");
        assertThat(username.getId()).isEqualTo("username");
        assertThat(username.isOptional()).isFalse();
        assertThat(username.isPersist()).isFalse();
        assertThat(password.getDisplayName()).isEqualTo("password");
        assertThat(password.getId()).isEqualTo("password");
        assertThat(password.getRegex()).isEqualTo(".*");
        assertThat(password.isOptional()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldPreAuthorizeConsentAndReturnFormStepWithOTPTextField(UrlDataProvider dataProvider) throws JsonProcessingException {
        //given
        UUID userId = UUID.randomUUID();
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("username", "login");
        filledInUserSiteFormValues.add("password", "12345");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManagerMock)
                .setState(UUID.randomUUID().toString())
                .setSigner(SIGNER)
                .setUserId(userId)
                .setPsuIpAddress("255.255.255.255")
                .build();
        //when
        FormStep formStep = (FormStep) dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();
        //then
        Form form = formStep.getForm();
        TextField otp = (TextField) form.getFormComponents().get(0);
        DKBAccessMeans dkbAccessMeansFromProviderState = objectMapper.readValue(formStep.getProviderState(), DKBAccessMeans.class);
        assertThat(form.getFormComponents().size()).isEqualTo(1);
        assertThat(formStep.getTimeoutTime()).isCloseTo(Instant.now().plusSeconds(300), byLessThan(30, ChronoUnit.SECONDS));
        assertThat(form.getExplanationField().getExplanation()).isEqualTo("0030 - Auftrag empfangen - Bitte die empfangene TAN eingeben.(MBT62820200002)");
        assertThat(form.getExplanationField().getDisplayName()).isEqualTo("explanation");
        assertThat(form.getExplanationField().getId()).isEqualTo("explanation");
        assertThat(otp.getDisplayName()).isEqualTo("otp");
        assertThat(otp.getId()).isEqualTo("otp");
        assertThat(otp.isOptional()).isFalse();
        assertThat(otp.isPersist()).isFalse();
        assertThat(dkbAccessMeansFromProviderState.getConsentId()).isEqualTo("7eab3784-04af-11ec-9a03-0242ac130003");
        assertThat(dkbAccessMeansFromProviderState.getEmbeddedToken()).isEqualTo("PreToken");
        assertThat(dkbAccessMeansFromProviderState.getAuthorisationId()).isEqualTo("123auth456");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldAuthorizeConsentWithOTPAndReturnSerializedDKBAccessMeans(UrlDataProvider dataProvider) throws JsonProcessingException {
        //given
        UUID userId = UUID.randomUUID();
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("otp", "push123otp");
        var dkbAccessMeans = new DKBAccessMeans("PreToken", "7eab3784-04af-11ec-9a03-0242ac130003", "123auth456");
        String serializedDKBAccessMeans = objectMapper.writeValueAsString(dkbAccessMeans);
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManagerMock)
                .setState(UUID.randomUUID().toString())
                .setSigner(SIGNER)
                .setUserId(userId)
                .setPsuIpAddress("255.255.255.255")
                .setProviderState(serializedDKBAccessMeans)
                .build();
        //when
        AccessMeansDTO accessMeansDTO = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getAccessMeans();
        //then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);
        assertThat(accessMeansDTO.getExpireTime()).isCloseTo(Date.from(Instant.now().plusSeconds(300)), 300);
        assertThat(accessMeansDTO.getUpdated()).isCloseTo(Date.from(Instant.now()), 300);
        assertThat(accessMeansDTO.getAccessMeans()).isEqualTo(serializedDKBAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldSendRequestToConsentAsAutoonboarding(AutoOnboardingProvider autoOnboardingProvider) {
        //given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                restTemplateManagerMock,
                SIGNER,
                Collections.singletonList("https://www.yolt.com/callback"),
                null);
        //when
        Map<String, BasicAuthenticationMean> autoConfigureMeans = autoOnboardingProvider.autoConfigureMeans(urlAutoOnboardingRequest);
        //then
        assertThat(autoConfigureMeans).isNullOrEmpty();
    }
}
