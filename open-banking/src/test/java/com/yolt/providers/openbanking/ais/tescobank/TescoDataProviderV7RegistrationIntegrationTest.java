package com.yolt.providers.openbanking.ais.tescobank;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
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

import static com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tescobank")
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/register", port = 0, httpsPort = 0)
class TescoDataProviderV7RegistrationIntegrationTest {

    private static final String REDIRECT_URL = "https://www.test-url.com/";
    private static final Signer SIGNER = new SignerMock();
    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("TescoBankDataProviderV7")
    private AutoOnboardingProvider tescoBankDataProviderV7;

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans();
    }

    private Stream<AutoOnboardingProvider> getProviders() {
        return Stream.of(tescoBankDataProviderV7);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewRegistration(AutoOnboardingProvider provider) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = provider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldNotCreateNewRegistration(AutoOnboardingProvider provider) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans, restTemplateManagerMock, SIGNER, RegistrationOperation.CREATE, null);

        // when
        Map<String, BasicAuthenticationMean> configureMeans = provider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("someClientId");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldUpdateRegistration(AutoOnboardingProvider provider) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans,
                restTemplateManagerMock,
                SIGNER,
                RegistrationOperation.UPDATE,
                null,
                Collections.singletonList(REDIRECT_URL),
                Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS));

        // when
        Map<String, BasicAuthenticationMean> configureMeans = provider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }
}
