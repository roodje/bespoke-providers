package com.yolt.providers.openbanking.ais.monzogroup.ais.V5;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupBasePaymentProviderV2;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("monzogroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/register", port = 0, httpsPort = 0)
public class MonzoGroupProviderRegistrationIntegrationTestV2 {

    private static final Signer SIGNER = new SignerMock();
    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("MonzoDataProviderV5")
    private MonzoGroupBaseDataProvider monzoDataProviderV5;

    @Autowired
    @Qualifier("MonzoPaymentProviderV5")
    private MonzoGroupBasePaymentProviderV2 monzoPaymentProviderV5;

    private Stream<AutoOnboardingProvider> getProviders() {
        return Stream.of(monzoDataProviderV5, monzoPaymentProviderV5);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRegister(AutoOnboardingProvider subject) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList("https://yolt.com/callback-test"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(registerMeans).doesNotContainKey(CLIENT_ID_NAME);
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
    }
}
