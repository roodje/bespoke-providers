package com.yolt.providers.unicredit.hypovereinsbank;

import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/hypovereins/register-404", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class HypoVereinsbankDataProviderRegistration404IntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Mock
    private Signer signer;

    @Qualifier("HypoVereinsbankDataProvider")
    @Autowired
    private UniCreditDataProvider dataProvider;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldThrowProviderHttpStatusExceptionForAutoConfigureMeansWhenNotFoundOnRegisterAPICall() {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequest(
                testAuthenticationMeans.getAuthMeans(), restTemplateManager, signer, null);

        // when
        ThrowableAssert.ThrowingCallable autoConfigureMeansCallable = () -> dataProvider.autoConfigureMeans(request);

        // then
        assertThatThrownBy(autoConfigureMeansCallable)
                .isInstanceOf(ProviderHttpStatusException.class);
    }
}
