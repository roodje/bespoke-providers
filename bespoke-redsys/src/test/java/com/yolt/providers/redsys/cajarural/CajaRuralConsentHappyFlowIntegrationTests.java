package com.yolt.providers.redsys.cajarural;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.redsys.RedsysSampleAuthenticationMeans;
import com.yolt.providers.redsys.TestConfiguration;
import com.yolt.providers.redsys.cajarural.consentretrieval.CajaRuralSerializableConsentProcessData;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.common.newgeneric.ResdysGenericStepDataProvider;
import com.yolt.providers.redsys.common.newgeneric.SerializableConsentProcessData;
import com.yolt.providers.redsys.mock.RestTemplateManagerMock;
import com.yolt.providers.redsys.mock.SignerMock;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.SelectField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains fetch data happy flow occurring in Cajarural provider.
 * Covered flows:
 * - fetching accounts, balances, transactions
 * <p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/cajarural-1.0.0", httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
@ActiveProfiles("redsys")
class CajaRuralConsentHappyFlowIntegrationTests {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String PSU_IP_ADDRESS = "192.168.16.5";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE = "https://www.yolt.com/callback?test&code=7d3ab3a5-12f5-11eb-8302-711ae25e53c6";
    private static final String ACCESS_TOKEN = "4ebf95f7-02f5-11eb-8309-411ae25e53e3";
    private static final String REFRESH_TOKEN = "1d3aa3a4-02f5-12eb-8309-711ae25e53e6";
    private static final String CONSENT_ID = "7eea2874-04f5-21ec-9cd5-fbg83ac17655";
    private static final String CODE_VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

    @Autowired
    @Qualifier("CajaRuralDataProviderV1")
    private ResdysGenericStepDataProvider dataProvider;

    @Autowired
    @Qualifier("Redsys")
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    private RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock();
    private SignerMock signerMock = new SignerMock();

    private RedsysSampleAuthenticationMeans sampleAuthenticationMeans = new RedsysSampleAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getRedsysSampleAuthenticationMeans();
    }

    private final RuralBank aspspInStubs = RuralBank.ALBAL;

    @Test
    public void shouldReturnFormStepWithRuralBanks() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("8b6dee15-ea2a-49b2-b100-f5f96d31cd90")
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .build();
        // when
        FormStep formStep = (FormStep) dataProvider.getLoginInfo(request);
        // then
        assertThat(((SelectField) formStep.getForm().getFormComponents().get(0)).getSelectOptionValues().size()).isEqualTo(RuralBank.values().length);
        assertThat(((SelectField) formStep.getForm().getFormComponents().get(0)).getId()).isEqualTo("region");
    }

    @Test
    public void shouldReturnRedirectStepWithConsentUrl() throws JsonProcessingException {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("region", aspspInStubs.name());
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("8b6dee15-ea2a-49b2-b100-f5f96d31cd90")
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setProviderState(objectMapper.writeValueAsString(new CajaRuralSerializableConsentProcessData()))
                .build();

        // when
        AccessMeansOrStepDTO accessMeansResponse = dataProvider.createNewAccessMeans(request);

        // then
        RedirectStep redirectStep = (RedirectStep) accessMeansResponse.getStep();
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains(aspspInStubs.name() + "/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("response_type", "code")
                .containsEntry("client_id", "redsysClientId")
                .containsEntry("redirect_uri", "https%3A%2F%2Fwww.yolt.com%2Fcallback")
                .containsEntry("scope", "AIS")
                .containsEntry("state", "8b6dee15-ea2a-49b2-b100-f5f96d31cd90")
                .containsKey("code_challenge")
                .containsKey("code_challenge_method");

        CajaRuralSerializableConsentProcessData consentProcessData = objectMapper.readValue(redirectStep.getProviderState(), CajaRuralSerializableConsentProcessData.class);
        assertThat(consentProcessData.getAspspName()).isEqualTo(aspspInStubs.name());
        assertThat(consentProcessData.getConsentStepNumber()).isEqualTo(1);
        assertThat(consentProcessData.getAccessMeans().getCodeVerifier()).isNotBlank();

    }

    @Test
    public void shouldReturnConfirmConsentPageAfterUserAuthorizes() throws JsonProcessingException {
        // given
        RedsysAccessMeans accessMeans = new RedsysAccessMeans(null);
        accessMeans.setCodeVerifier(CODE_VERIFIER);
        CajaRuralSerializableConsentProcessData consentProcessData = new CajaRuralSerializableConsentProcessData();
        consentProcessData.setConsentStepNumber(1);
        consentProcessData.setAspspName(aspspInStubs.name());
        consentProcessData.setAccessMeans(accessMeans);

        String redirectUrl = REDIRECT_URL + "?code=7d3ab3a5-12f5-11eb-8302-711ae25e53c6&state=8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setProviderState(objectMapper.writeValueAsString(consentProcessData))
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(result.getStep()).isNotNull();
        assertThat(result.getStep().getProviderState()).isNotNull();

        CajaRuralSerializableConsentProcessData providerState = objectMapper.readValue(result.getStep().getProviderState(), CajaRuralSerializableConsentProcessData.class);
        assertThat(providerState.getAspspName()).isEqualTo(aspspInStubs.name());
        assertThat(providerState.getConsentStepNumber()).isEqualTo(2);
        assertThat(providerState.getAccessMeans().getCodeVerifier()).isEqualTo(CODE_VERIFIER);
        assertThat(providerState.getAccessMeans().getRedirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(providerState.getAccessMeans().getConsentId()).isNotBlank();
    }

    @Test
    public void shouldReturnAccessMeansOnCreateAccessMeansWhenConsentIdIsGiven() throws TokenInvalidException, JsonProcessingException {
        // given
        RedsysAccessMeans accessMeans = new RedsysAccessMeans(CODE_VERIFIER);
        CajaRuralSerializableConsentProcessData consentProcessData = new CajaRuralSerializableConsentProcessData();
        consentProcessData.setConsentStepNumber(2);
        consentProcessData.setAspspName(aspspInStubs.name());
        consentProcessData.setAccessMeans(accessMeans);
        accessMeans.setConsentId(CONSENT_ID);
        accessMeans.setRedirectUrl(REDIRECT_URL);
        Token token = new Token();
        token.setExpiresIn(3600);
        token.setAccessToken(ACCESS_TOKEN);
        token.setRefreshToken(REFRESH_TOKEN);
        accessMeans.setToken(token);

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setSigner(signerMock)
                .setProviderState(objectMapper.writeValueAsString(consentProcessData))
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        Step step = result.getStep();
        assertThat(step).isNull();
        AccessMeansDTO accessMeansDTO = result.getAccessMeans();
        RedsysAccessMeans redsysAccessMeans = deserializeAccessMeans(accessMeansDTO.getAccessMeans()).getAccessMeans();
        assertThat(accessMeansDTO).isNotNull();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(redsysAccessMeans.getToken().getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(redsysAccessMeans.getToken().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(redsysAccessMeans.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(redsysAccessMeans.getCodeVerifier()).isEqualTo(CODE_VERIFIER);
        assertThat(redsysAccessMeans.getRedirectUrl()).isEqualTo(REDIRECT_URL);
    }

    @Test
    void shouldReturnNewAccessMeansForRefreshAccessMeans() throws TokenInvalidException, JsonProcessingException {
        // given
        Token token = new Token();
        token.setRefreshToken("1d3aa3a4-02f5-12eb-8309-711ae25e53e6");
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(
                        new SerializableConsentProcessData(3,
                                new RedsysAccessMeans(token, REDIRECT_URL, CONSENT_ID, null, Instant.MIN, new FilledInUserSiteFormValues()),
                                RuralBank.ALBAL.name())),
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(3600)));

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .build();

        // then
        AccessMeansDTO result = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);

        RedsysAccessMeans refreshTokenResponse = objectMapper.readValue(result.getAccessMeans(), CajaRuralSerializableConsentProcessData.class).getAccessMeans();
        assertThat(refreshTokenResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(refreshTokenResponse.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(refreshTokenResponse.getToken().getRefreshToken()).isEqualTo("Brand_new_refresh_token");
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(5)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(SIGNING_KEY_ID_NAME, KEY_ID)
                .containsEntry(SIGNING_CERTIFICATE_NAME, CLIENT_SIGNING_CERTIFICATE_PEM)
                .containsEntry(TRANSPORT_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CLIENT_TRANSPORT_CERTIFICATE_PEM);
    }

    private CajaRuralSerializableConsentProcessData deserializeAccessMeans(final String accessMean) throws TokenInvalidException {
        CajaRuralSerializableConsentProcessData consentProcessData;
        try {
            consentProcessData = objectMapper.readValue(accessMean, CajaRuralSerializableConsentProcessData.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means.");
        }
        return consentProcessData;
    }
}
