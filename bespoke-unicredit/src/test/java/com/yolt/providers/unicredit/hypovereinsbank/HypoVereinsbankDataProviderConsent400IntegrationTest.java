package com.yolt.providers.unicredit.hypovereinsbank;

import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/hypovereins/consent-400", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class HypoVereinsbankDataProviderConsent400IntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final String PSU_IP_ADDRESS = "10.0.0.2";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Qualifier("HypoVereinsbankDataProvider")
    @Autowired
    private UniCreditDataProvider dataProvider;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldThrowProviderHttpStatusExceptionForGetLoginInfoWhenBadRequestOnPostConsentAPICall() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(UUID.randomUUID().toString())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable getLoginInfoCallable = () -> dataProvider.getLoginInfo(request);

        // then
        // ProviderHttpStatusException
        assertThatThrownBy(getLoginInfoCallable)
                .isInstanceOf(ProviderHttpStatusException.class);
    }
}
