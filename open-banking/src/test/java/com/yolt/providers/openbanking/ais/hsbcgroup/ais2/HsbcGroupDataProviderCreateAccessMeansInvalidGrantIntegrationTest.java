package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.HsbcGroupBaseDataProviderV7;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case when create access means method was called with wrong parameter returned from consent redirect url.
 * For such case we want to throw {@link GetAccessTokenFailedException})
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
class HsbcGroupDataProviderCreateAccessMeansInvalidGrantIntegrationTest {

    private static final SignerMock SIGNER = new SignerMock();
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");

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
    void shouldThrowGetAccessTokenFailedExceptionWhenCreateNewAccessMeansWithWrongQueryParametersFromRedirectUrl(UrlDataProvider provider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545?error=invalid_grant")
                .setSigner(SIGNER)
                .setProviderState("""
                        {"permissions":["ReadParty",\
                        "ReadAccountsDetail",\
                        "ReadBalances",\
                        "ReadDirectDebits",\
                        "ReadProducts",\
                        "ReadStandingOrdersDetail",\
                        "ReadTransactionsCredits",\
                        "ReadTransactionsDebits",\
                        "ReadTransactionsDetail"]}\
                        """)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}
