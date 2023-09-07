package com.yolt.providers.openbanking.ais.bankofirelandgroup.ais;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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

import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper.CLIENT_ID_NAME_V2;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bankofireland")
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankofireland/register", port = 0, httpsPort = 0)
class BankOfIrelandDataProviderRegistrationIntegrationTest {

    private static final String REDIRECT_URL = "https://yolt.com/callback-test";
    private static final Signer SIGNER = new SignerMock();
    private static RestTemplateManagerMock REST_TEMPLATE_MANAGER;

    @Autowired
    @Qualifier("BankOfIrelandDataProviderV7")
    private AutoOnboardingProvider bankOfIrelandDataProviderV7;

    private Stream<Arguments> getProvidersWithSampleAuthMeans() {
        return Stream.of(
                Arguments.of(bankOfIrelandDataProviderV7, BankOfIrelandSampleTypedAuthMeans.getSampleAuthMeans())
        );
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        REST_TEMPLATE_MANAGER = new RestTemplateManagerMock(() -> "12345");
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    void shouldCreateNewRegistration(AutoOnboardingProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(CLIENT_ID_NAME_V2);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(CLIENT_ID_NAME_V2)).isTrue();
        assertThat(configureMeans.get(CLIENT_ID_NAME_V2).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    void shouldNotCreateNewRegistration(AutoOnboardingProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans, REST_TEMPLATE_MANAGER, SIGNER, RegistrationOperation.CREATE, null);

        // when
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(CLIENT_ID_NAME_V2)).isTrue();
        assertThat(configureMeans.get(CLIENT_ID_NAME_V2).getValue()).isEqualTo("someClientId");
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    void shouldUpdateRegistration(AutoOnboardingProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans,
                REST_TEMPLATE_MANAGER,
                SIGNER,
                RegistrationOperation.UPDATE,
                null,
                Collections.singletonList(REDIRECT_URL),
                Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS));

        // when
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(CLIENT_ID_NAME_V2)).isTrue();
        assertThat(configureMeans.get(CLIENT_ID_NAME_V2).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }
}
