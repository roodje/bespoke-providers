package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.ais;

import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.VirginMoney2DataProviderV1;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2App;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2JwsSigningResult;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2SampleAuthenticationMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * This test suite contains unhappy flow occurring in Virgin Money (Merged APIs) provider.
 * Covered flows:
 * - 400 when acquiring consent page
 * <p>
 */
@SpringBootTest(classes = {VirginMoney2App.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney2")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/virginmoney2group/virginmoney2/ais/v3.1.2/account-access-consent_400",
        "classpath:/stubs/virginmoney2group/virginmoney2/oauth2/v3.0/happy-flow"},
        httpsPort = 0,
        port = 0)
public class VirginMoney2DataProviderAccountConsent400IntegrationTest {

    @Autowired
    private VirginMoney2DataProviderV1 virginMoney2DataProvider;

    @Mock
    private Signer signer;

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Test
    void shouldThrowLoginNotFoundExceptionWhen400ResponseIsReceivedFromAccountConsentEndpoint() throws IOException, URISyntaxException {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setState("state")
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("http://yolt.com/redirect")
                .build();

        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(new VirginMoney2JwsSigningResult());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> virginMoney2DataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThatExceptionOfType(LoginNotFoundException.class)
                .isThrownBy(throwingCallable)
                .withMessageContaining("400 Bad Request");
    }
}
