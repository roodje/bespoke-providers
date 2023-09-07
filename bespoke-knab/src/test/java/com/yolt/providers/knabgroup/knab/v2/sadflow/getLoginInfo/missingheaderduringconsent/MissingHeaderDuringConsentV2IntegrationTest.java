package com.yolt.providers.knabgroup.knab.v2.sadflow.getLoginInfo.missingheaderduringconsent;

import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
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
@AutoConfigureWireMock(stubs = "classpath:/stubs/sadflowv2/getlogininfo/missingheaderduringconsent", httpsPort = 0, port = 0)
public class MissingHeaderDuringConsentV2IntegrationTest {


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
    public void shouldThrowGetLoginInfoUrlFailedExceptionWhenOneHeaderIsMissing() {
        // given
        String state = UUID.randomUUID().toString();
        String baseClientRedirectUrl = "https://www.yolt.com/callback-dev";
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setState(state)
                .setPsuIpAddress("127.0.0.1")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable getLoginInfoThrowable = () -> urlDataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThatThrownBy(getLoginInfoThrowable)
                .isExactlyInstanceOf(GetLoginInfoUrlFailedException.class)
                .hasMessage("Request formed incorrectly: HTTP 400");
    }
}
