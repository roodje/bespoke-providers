package com.yolt.providers.openbanking.ais.aibgroup;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AibGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("aib")
@AutoConfigureWireMock(stubs = "classpath:/stubs/aibgroup/v31/client_secret/account-access-consent-500", httpsPort = 0, port = 0)
public class AibGroupDataProviderOnUserSiteDelete500IntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> UUID.randomUUID().toString());

    @Autowired
    @Qualifier("AibDataProviderV6")
    private GenericBaseDataProvider aibDataProviderV5;

    @Autowired
    @Qualifier("AibNIDataProviderV6")
    private GenericBaseDataProvider aibNIDataProviderV6;

    @Autowired
    @Qualifier("AibIeDataProviderV1")
    private GenericBaseDataProvider aibIeDataProviderV1;

    private Stream<Arguments> getAibProviders() {
        return Stream.of(
                Arguments.of(aibDataProviderV5, "/v3.1"),
                Arguments.of(aibNIDataProviderV6, "/v3.1"),
                Arguments.of(aibIeDataProviderV1, "/v3.1"));
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = AibGroupSampleAuthenticationMeans.getAibGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldNotPerformOnUserSiteDeleteWhenErrorWasReturned(GenericBaseDataProvider aibDataProvider, String endpointVersion) {
        // given
        String externalConsentId = "externalConsentId";
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        assertThatThrownBy(() -> aibDataProvider.onUserSiteDelete(urlGetLogin))
                .isExactlyInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
