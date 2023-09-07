package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.ais;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.VirginMoney2DataProviderV1;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2App;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2JwsSigningResult;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2SampleAuthenticationMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * This test suite contains unhappy flow occurring in Virgin Money (Merged APIs) provider.
 * Covered flows:
 * - 400 for autoonboarding
 * <p>
 */
@SpringBootTest(classes = {VirginMoney2App.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney2")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/virginmoney2group/virginmoney2/registration/v3.2/error_400"},
        httpsPort = 0,
        port = 0)
public class VirginMoney2DataProviderAutoOnboarding400IntegrationTest {

    @Autowired
    private VirginMoney2DataProviderV1 virginMoney2DataProvider;

    @Mock
    private Signer signer;

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Test
    void shouldThrowAutoOnboardingExceptionWhen4xxResponseFromDCREndpointIsReceived() throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAutoonboarding();
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setRedirectUrls(List.of("http://redirect1", "http://redirect2"))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS))
                .build();

        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(new VirginMoney2JwsSigningResult());

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> virginMoney2DataProvider.autoConfigureMeans(autoOnboardingRequest);

        // then
        assertThatExceptionOfType(AutoOnboardingException.class)
                .isThrownBy(throwingCallable)
                .withMessage("Auto-onboarding failed for VIRGIN_MONEY_MERGED_APIS, message=Auto-onboarding failed for Virgin Money Bank")
                .withCauseInstanceOf(HttpClientErrorException.BadRequest.class);
    }
}
