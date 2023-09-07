package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.HsbcGroupBaseDataProviderV7;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request acquiring accounts fail due to 500.
 * This means that request there was internal server error, thus we can't map such account (so throw {@link ProviderFetchDataException})
 * <p>
 * Disclaimer: most providers in HSBC group are the same from code and stubs perspective (then only difference is configuration)
 * The only difference is for balance types in HSBC Corporate provider. Due to that fact this test class is parametrised,
 * so all providers in group are tested.
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/hsbcgroup/ais-3.1.6"}, httpsPort = 0, port = 0)
class HsbcGroupDataProviderFetchData500IntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;
    private static final SignerMock SIGNER = new SignerMock();

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Instant FETCH_FROM = Instant.parse("2020-01-01T00:00:00Z");
    private static final String ACCESS_TOKEN = "VRnKXR6gUjzWazlPdhUVHZGv2C3";
    private static final String REFRESH_TOKEN = "NfEtxxaLt1SavZW1s7thJ7iw0XZ";

    @Autowired
    @Qualifier("HsbcDataProviderV13")
    private HsbcGroupBaseDataProviderV7 hsbcDataProviderV13;

    @Autowired
    @Qualifier("MarksAndSpencerDataProviderV13")
    private HsbcGroupBaseDataProviderV7 marksAndSpencerDataProviderV13;

    @Autowired
    @Qualifier("FirstDirectDataProviderV13")
    private HsbcGroupBaseDataProviderV7 firstDirectDataProviderV13;

    @Autowired
    @Qualifier("HsbcCorpoDataProviderV11")
    private HsbcGroupBaseDataProviderV7 hsbcCorpoDataProviderV11;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                hsbcDataProviderV13,
                firstDirectDataProviderV13,
                hsbcCorpoDataProviderV11,
                marksAndSpencerDataProviderV13);
    }


    private String requestTraceId;
    private RestTemplateManager restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private HsbcGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new HsbcGroupSampleAuthenticationMeansV2();

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        requestTraceId = "d10f24f4-032a-4843-bfc9-22b599c7ae2d";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = sampleAuthenticationMeans.getHsbcGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowProviderFetchDataExceptionWhenAccountsRequestFail(UrlDataProvider provider)
            throws JsonProcessingException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createToken();
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(hsbcGroupAccessMeans));

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(urlFetchDataRequest);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderFetchDataException.class);
    }

    private AccessMeansState<HsbcGroupAccessMeansV2> createToken() {
        return new AccessMeansState<>(new HsbcGroupAccessMeansV2(
                Instant.now(),
                USER_ID,
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                Date.from(Instant.now().plus(1, DAYS)),
                Date.from(Instant.now()),
                "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
    }

    private AccessMeansDTO createAccessMeansDTO(AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken) throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                serializeToken(oAuthToken),
                new Date(),
                Date.from(Instant.now().plus(1, DAYS)));
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(final AccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(FETCH_FROM)
                .setAccessMeans(accessMeansDTO)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();
    }

    private String serializeToken(final AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(oAuthToken);
    }
}
