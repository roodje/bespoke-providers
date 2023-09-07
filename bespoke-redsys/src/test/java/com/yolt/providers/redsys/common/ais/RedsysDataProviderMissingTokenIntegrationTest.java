package com.yolt.providers.redsys.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
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
import com.yolt.providers.redsys.ibercaja.IbercajaDataProvider;
import com.yolt.providers.redsys.kutxabank.KutxabankDataProviderV1;
import com.yolt.providers.redsys.mock.RestTemplateManagerMock;
import com.yolt.providers.redsys.mock.SignerMock;
import com.yolt.providers.redsys.openbank.OpenbankDataProviderV2;
import com.yolt.providers.redsys.sabadell.SabadellDataProviderV3;
import com.yolt.providers.redsys.santander.SantanderESDataProviderV3;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.assertj.core.api.ThrowableAssert;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case which occurs when consent is expired. On refresh token step we receive 200 response code, but
 * tokens are missing in response body.
 * This means that we have to force user to perform new consent step (so throw {@link TokenInvalidException})
 * <p>
 * Disclaimer: Tests are parametrized and run for all providers in group:
 * {@link BankinterDataProviderV4}, {@link BBVADataProviderV3}, {@link CaixaDataProviderV4}, {@link SabadellDataProviderV3},
 * {@link SantanderESDataProviderV3}, {@link CajasurDataProviderV1}, {@link KutxabankDataProviderV1}, {@link OpenbankDataProviderV2},
 * {@link CajamarCajaRuralDataProviderV1}
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/", httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
@ActiveProfiles("redsys")
class RedsysDataProviderMissingTokenIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String ACCESS_TOKEN = "00f707ea-ed95-4883-9ffc-6139150416f7";
    private static final String REFRESH_TOKEN = "437f67ad-c0a8-419a-a868-7a3296a15ccf";
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
                sabadellDataProvider, santanderESDataProvider, cajasurDataProviderV1, kutxabankDataProviderV1, openbankDataProviderV2,
                ibercajaDataProvider, cajamarCajaRuralDataProviderV1);
    }

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getRedsysSampleAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getRedsysProviders")
    public void shouldThrowTokenInvalidExceptionForRefreshAccessMeansWithMissingRefreshToken(UrlDataProvider provider) throws IOException {
        // given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> provider.refreshAccessMeans(request);

        //then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(new RedsysAccessMeans(createAccessMeansToken(), REDIRECT_URL, CONSENT_ID, null, Instant.MIN, new FilledInUserSiteFormValues())),
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(3600)));
    }

    private static Token createAccessMeansToken() {
        Token token = new Token();
        token.setAccessToken(ACCESS_TOKEN);
        token.setRefreshToken(REFRESH_TOKEN);
        token.setTokenType("Bearer");
        token.setExpiresIn(3600);
        return token;
    }
}
