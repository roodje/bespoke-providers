package com.yolt.providers.openbanking.ais.aibgroup;

import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Due to the unique set of permissions accepted by AIB_IE getLoginInfo requires separate stub
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AibGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("aib")
@AutoConfigureWireMock(stubs = "classpath:/stubs/aibgroup/v31/client_secret/aib-ie-accounts-access-consent/", httpsPort = 0, port = 0)
public class AibIeDataProviderGetLoginInfoIntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> UUID.randomUUID().toString());

    @Autowired
    @Qualifier("AibIeDataProviderV1")
    private GenericBaseDataProvider aibIeDataProviderV1;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = AibGroupSampleAuthenticationMeans.getAibGroupSampleAuthenticationMeansForAis();
    }

    @Test
    void shouldReturnCorrectRedirectStepForGetLoginInfoWithCorrectRequestData() {
        // given
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) aibIeDataProviderV1.getLoginInfo(urlGetLogin);

        // then
        String expectedUrlRegex = ".*\\/authorize\\?response_type=code\\+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=openid\\+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt\\.com%2Fidentifier&request=.*";
        assertThat(loginInfo.getRedirectUrl()).matches(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("363ca7c1-9d03-4876-8766-ddefc9fd2d76");
    }

}