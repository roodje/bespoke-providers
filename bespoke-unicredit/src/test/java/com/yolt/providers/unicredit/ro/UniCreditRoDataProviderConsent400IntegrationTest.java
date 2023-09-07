package com.yolt.providers.unicredit.ro;

import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/ro/consent-400", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class UniCreditRoDataProviderConsent400IntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
    private static final String PSU_IP_ADDRESS = "10.0.0.2";
    private static final String IBAN_FORM_FIELD_ID = "Iban";
    private static final String PSU_IBAN = "IT18L0200811770000019486580";

    @Mock
    private Signer signer;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Qualifier("UniCreditRoDataProviderV1")
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
        UUID userId = UUID.randomUUID();
        Map<String, String> filledInFormFieldsValues = new HashMap<>();
        filledInFormFieldsValues.put(IBAN_FORM_FIELD_ID, PSU_IBAN);
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(filledInFormFieldsValues);
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(STATE)
                .build();

        // when
        ThrowableAssert.ThrowingCallable getLoginInfoCallable = () -> dataProvider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(getLoginInfoCallable)
                .isInstanceOf(ProviderHttpStatusException.class);
    }
}
