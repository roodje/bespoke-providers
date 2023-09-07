package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.exception.ProviderRequestFailedException;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * When there are some server issue on bank side, we want to retry the deletion in future, thus we throw {@link ProviderRequestFailedException})
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
class HsbcGroupDataProviderDeleteUserSite500IntegrationTest {

    private static final SignerMock SIGNER = new SignerMock();

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
    void shouldThrowProviderRequestFailedExceptionWhenUserSiteDeleteWithFails(UrlDataProvider provider) {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("ae24a1ae-61a4-11e9-8647-d663bd873d93")
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).isExactlyInstanceOf(ProviderRequestFailedException.class);
    }
}
