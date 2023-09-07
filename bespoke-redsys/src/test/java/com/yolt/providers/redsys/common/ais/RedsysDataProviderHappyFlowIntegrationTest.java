package com.yolt.providers.redsys.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.redsys.RedsysSampleAuthenticationMeans;
import com.yolt.providers.redsys.TestConfiguration;
import com.yolt.providers.redsys.bankinter.BankinterDataProviderV4;
import com.yolt.providers.redsys.bbva.BBVADataProviderV3;
import com.yolt.providers.redsys.caixa.CaixaDataProviderV4;
import com.yolt.providers.redsys.cajamarcajarural.CajamarCajaRuralDataProviderV1;
import com.yolt.providers.redsys.cajasur.CajasurDataProviderV1;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.evo.EvoDataProvider;
import com.yolt.providers.redsys.ibercaja.IbercajaDataProvider;
import com.yolt.providers.redsys.kutxabank.KutxabankDataProviderV1;
import com.yolt.providers.redsys.laboralkutxa.LaboralKutxaDataProvider;
import com.yolt.providers.redsys.mock.RestTemplateManagerMock;
import com.yolt.providers.redsys.mock.SignerMock;
import com.yolt.providers.redsys.openbank.OpenbankDataProviderV2;
import com.yolt.providers.redsys.sabadell.SabadellDataProviderV3;
import com.yolt.providers.redsys.santander.SantanderESDataProviderV3;
import com.yolt.providers.redsys.unicaja.UnicajaDataProvider;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Redsys group providers.
 * <p>
 * Disclaimer: Tests are parametrized and run for all providers in group:
 * {@link BankinterDataProviderV4}, {@link BBVADataProviderV3}, {@link CaixaDataProviderV4}, {@link SabadellDataProviderV3},
 * {@link SantanderESDataProviderV3}, {@link CajasurDataProviderV1}, {@link KutxabankDataProviderV1}, {@link OpenbankDataProviderV2}
 * {@link EvoDataProvider}, {@link UnicajaDataProvider}, {@link LaboralKutxaDataProvider} , {@link CajamarCajaRuralDataProviderV1}
 * Due to the fact that there are differences data returned on fetch step, this flow is tested in dedicated classes.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/", httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
@ActiveProfiles("redsys")
class RedsysDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String PSU_IP_ADDRESS = "192.168.16.5";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String ACCESS_TOKEN = "4ebf95f7-02f5-11eb-8309-411ae25e53e3";
    private static final String REFRESH_TOKEN = "1d3aa3a4-02f5-12eb-8309-711ae25e53e6";
    private static final String CONSENT_ID = "7eea2874-04f5-21ec-9cd5-fbg83ac17655";

    @Autowired
    private BankinterDataProviderV4 bankinterDataProvider;

    @Autowired
    private BBVADataProviderV3 bbvaDataProvider;

    @Autowired
    private CaixaDataProviderV4 caixaDataProvider;

    @Autowired
    private SabadellDataProviderV3 sabadellDataProvider;

    @Autowired
    private SantanderESDataProviderV3 santanderESDataProvider;

    @Autowired
    private CajasurDataProviderV1 cajasurDataProviderV1;

    @Autowired
    private OpenbankDataProviderV2 openbankDataProviderV2;

    @Autowired
    private KutxabankDataProviderV1 kutxabankDataProviderV1;

    @Autowired
    private LaboralKutxaDataProvider laboralKutxaDataProvider;

    @Autowired
    private EvoDataProvider evoDataProvider;

    @Autowired
    private UnicajaDataProvider unicajaDataProvider;

    @Autowired
    private IbercajaDataProvider ibercajaDataProvider;

    @Autowired
    private CajamarCajaRuralDataProviderV1 cajamarCajaRuralDataProviderV1;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("Redsys")
    private ObjectMapper objectMapper;

    private RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock();
    private SignerMock signerMock = new SignerMock();

    private RedsysSampleAuthenticationMeans sampleAuthenticationMeans = new RedsysSampleAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    Stream<UrlDataProvider> getRedsysProviders() {
        return Stream.of(bankinterDataProvider, bbvaDataProvider, caixaDataProvider,
                sabadellDataProvider, santanderESDataProvider, cajasurDataProviderV1, kutxabankDataProviderV1,
                openbankDataProviderV2, evoDataProvider, laboralKutxaDataProvider, unicajaDataProvider, ibercajaDataProvider,
                cajamarCajaRuralDataProviderV1);
    }

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getRedsysSampleAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getRedsysProviders")
    void shouldReturnTypedAuthenticationMeans(UrlDataProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = provider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(5)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(SIGNING_KEY_ID_NAME, KEY_ID)
                .containsEntry(SIGNING_CERTIFICATE_NAME, CLIENT_SIGNING_CERTIFICATE_PEM)
                .containsEntry(TRANSPORT_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CLIENT_TRANSPORT_CERTIFICATE_PEM);
    }

    @ParameterizedTest
    @MethodSource("getRedsysProviders")
    void shouldReturnRedirectStepWithConsentUrl(UrlDataProvider provider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("8b6dee15-ea2a-49b2-b100-f5f96d31cd90")
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) provider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("response_type", "code")
                .containsEntry("client_id", "redsysClientId")
                .containsEntry("redirect_uri", REDIRECT_URL)
                .containsEntry("scope", "AIS")
                .containsEntry("state", "8b6dee15-ea2a-49b2-b100-f5f96d31cd90")
                .containsKey("code_challenge")
                .containsKey("code_challenge_method");
    }

    @ParameterizedTest
    @MethodSource("getRedsysProviders")
    void shouldReturnSCARedirectStepForCreateNewAccessMeansWhenNoConsentIdProvidedInAccessMeans(UrlDataProvider provider)
            throws JsonProcessingException {
        // given
        String redirectUrl = REDIRECT_URL + "?code=7d3ab3a5-12f5-11eb-8302-711ae25e53c6&state=8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
        String providerState = serializeAccessMeans(new RedsysAccessMeans("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setProviderState(providerState)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO result = provider.createNewAccessMeans(request);

        // then
        verify(postRequestedFor(urlMatching("\\/([a-z]*)\\/token\\?grant_type=authorization_code&client_id=redsysClientId&code_verifier=dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk&code=7d3ab3a5-12f5-11eb-8302-711ae25e53c6&redirect_uri=https:\\/\\/www\\.yolt\\.com\\/callback"))
                .withHeader("content-type", equalTo("application/x-www-form-urlencoded")));
        assertThat(result.getStep()).isNotNull();
        assertThat(result.getStep().getProviderState()).isNotNull();
        RedsysAccessMeans accessMeans = objectMapper.readValue(result.getStep().getProviderState(), RedsysAccessMeans.class);
        assertThat(accessMeans.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        validateAccessMeansToken(accessMeans.getToken());
    }

    @ParameterizedTest
    @MethodSource("getRedsysProviders")
    void shouldReturnNewAccessMeansForCreateNewAccessMeansWhenConsentIdProvidedInAccessMeans(UrlDataProvider provider) throws JsonProcessingException {
        // given
        String redirectUrl = REDIRECT_URL + "?state=8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
        Token token = createAccessMeansToken();
        RedsysAccessMeans redsysAccessMeans = new RedsysAccessMeans(token, REDIRECT_URL, CONSENT_ID, "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk", Instant.MIN, new FilledInUserSiteFormValues());
        String providerState = serializeAccessMeans(redsysAccessMeans);
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setProviderState(providerState)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO result = provider.createNewAccessMeans(request);

        // then
        RedsysAccessMeans accessMeans = objectMapper.readValue(result.getAccessMeans().getAccessMeans(), RedsysAccessMeans.class);
        assertThat(accessMeans.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(accessMeans.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(accessMeans.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        validateAccessMeansToken(accessMeans.getToken());
    }

    //TODO: This test literally tests nothing since refresh token we request is the very same we return and expect
    @ParameterizedTest
    @MethodSource("getRedsysProviders")
    void shouldReturnNewAccessMeansForRefreshAccessMeans(UrlDataProvider provider) throws TokenInvalidException, JsonProcessingException {
        // given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .build();

        // then
        AccessMeansDTO result = provider.refreshAccessMeans(request);

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);

        RedsysAccessMeans refreshTokenResponse = objectMapper.readValue(result.getAccessMeans(), RedsysAccessMeans.class);
        assertThat(refreshTokenResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(refreshTokenResponse.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        validateAccessMeansToken(refreshTokenResponse.getToken());
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(new RedsysAccessMeans(createAccessMeansToken(), REDIRECT_URL, CONSENT_ID, null, Instant.MIN, new FilledInUserSiteFormValues())),
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(3600)));
    }

    private String serializeAccessMeans(RedsysAccessMeans redsysAccessMeans) {
        try {
            return objectMapper.writeValueAsString(redsysAccessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize access means.");
        }
    }

    private static Token createAccessMeansToken() {
        Token token = new Token();
        token.setAccessToken(ACCESS_TOKEN);
        token.setRefreshToken(REFRESH_TOKEN);
        token.setTokenType("Bearer");
        token.setExpiresIn(3600);
        return token;
    }

    private void validateAccessMeansToken(Token token) {
        assertThat(token.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(token.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(token.getTokenType()).isEqualTo("Bearer");
        assertThat(token.getExpiresIn()).isNotNull();
    }
}
