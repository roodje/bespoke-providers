package com.yolt.providers.knabgroup.knab.v2.sadflow.createnewaccessmeans.invalidtoken;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestApp;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.common.KnabGroupDataProviderV2;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sadflowv2/createnewaccessmeans/invalidtoken", httpsPort = 0, port = 0)
class InvalidTokenV2IntegrationTest {

    private static final String PSU_IP_ADDRESS = "0.0.0.0";
    public static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback-dev";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("KnabDataProviderV2")
    private KnabGroupDataProviderV2 urlDataProvider;

    private RestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @BeforeEach
    public void beforeEach() {
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        signer = new TestSigner();
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhenTokenIsInvalid() {
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback-dev?code=my-awesome-authorization-code&scope=psd2%20offline_access%20AIS%3Amy-consent-id&state=random42")
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> urlDataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Request formed incorrectly: HTTP 400");
    }
}
