package com.yolt.providers.openbanking.ais.lloydsbankinggroup.ais;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.ProviderRequestFailedException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsGroupApp;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsSampleTypedAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * When there are some server issue on bank side, we want to retry the deletion in future, thus we throw {@link ProviderRequestFailedException})
 * <p>
 * Disclaimer: most providers in LBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised,
 * so all providers in group are tested.
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {LloydsGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lloydsgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lloydsbankinggroup/ais/user-site-delete-errors/", httpsPort = 0, port = 0)
public class LloydsGroupDataProviderUserSiteDelete500IntegrationTest {

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("BankOfScotlandDataProviderV10")
    private GenericBaseDataProvider bankOfScotlandDataProviderV10;
    @Autowired
    @Qualifier("BankOfScotlandCorpoDataProviderV8")
    private GenericBaseDataProvider bankOfScotlandCorpoDataProviderV8;
    @Autowired
    @Qualifier("HalifaxDataProviderV10")
    private GenericBaseDataProvider halifaxDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankDataProviderV10")
    private GenericBaseDataProvider lloydsBankDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankCorpoDataProviderV8")
    private GenericBaseDataProvider lloydsBankCorpoDataProviderV8;
    @Autowired
    @Qualifier("MbnaCreditCardDataProviderV6")
    private GenericBaseDataProvider mbnaCreditCardDataProviderV6;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private String requestTraceId;
    private final String externalConsentId = "363ca7c1-9d03-4876-8766-ddefc9fd2d76";

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new LloydsSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldOnUserSiteDeleteWhenHttp404ResponseShouldThrowHttpServerErrorException(UrlDataProvider dataProvider) {
        // given
        requestTraceId = "50000000-8cea-412d-b6ed-04aeb924eace";

        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                bankOfScotlandDataProviderV10, bankOfScotlandCorpoDataProviderV8,
                halifaxDataProviderV10, lloydsBankDataProviderV10,
                lloydsBankCorpoDataProviderV8, mbnaCreditCardDataProviderV6);
    }
}
