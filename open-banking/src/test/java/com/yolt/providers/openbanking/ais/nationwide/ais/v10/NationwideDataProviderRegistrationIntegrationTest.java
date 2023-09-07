package com.yolt.providers.openbanking.ais.nationwide.ais.v10;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideDataProviderV10;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3;
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

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("nationwide")
@AutoConfigureWireMock(stubs = "classpath:/stubs/nationwide/register", port = 0, httpsPort = 0)
class NationwideDataProviderRegistrationIntegrationTest {

    private static final String REDIRECT_URL = "https://yolt.com/callback-test";
    private static final Signer SIGNER = new SignerMock();
    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("NationwideDataProviderV11")
    private NationwideDataProviderV10 nationwideDataProviderV11;

    private Stream<AutoOnboardingProvider> getAutoonboardingProviders() {
        return Stream.of(nationwideDataProviderV11);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = new NationwideSampleAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getAutoonboardingProviders")
    void shouldCreateNewRegistration(AutoOnboardingProvider subject) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(NationwideAuthMeansBuilderV3.CLIENT_ID_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsAllEntriesOf(registerMeans);
        assertThat(configureMeans).containsKey(NationwideAuthMeansBuilderV3.CLIENT_ID_NAME);
        assertThat(configureMeans.get(NationwideAuthMeansBuilderV3.CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }

    @ParameterizedTest
    @MethodSource("getAutoonboardingProviders")
    void shouldUpdateRegistration(AutoOnboardingProvider subject) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans,
                restTemplateManagerMock,
                SIGNER,
                null,
                Collections.singletonList(REDIRECT_URL),
                Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS));

        // when
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsExactlyInAnyOrderEntriesOf(registerMeans);
    }
}
