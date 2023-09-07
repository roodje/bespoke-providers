package com.yolt.providers.openbanking.ais.tidegroup.register;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupApp;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupDataProviderV2;
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

import static com.yolt.providers.openbanking.ais.tidegroup.common.auth.TideGroupAuthMeansMapperV3.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains missing data flows occurring in Tide group providers. As a business decision provider implementation should return dto as best effort
 * Covered flows:
 * - fetching accounts, balances, transactions when some data are missing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TideGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tidegroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/tidegroup/register", port = 0, httpsPort = 0)
class TideGroupDataProviderRegistrationIntegrationTestV2 {

    private static final String REDIRECT_URL = "https://www.test-url.com/";
    private static final Signer SIGNER = new SignerMock();
    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("TideDataProviderV3")
    private TideGroupDataProviderV2 tideDataProvider;

    private Stream<TideGroupDataProviderV2> getProviders() {
        return Stream.of(tideDataProvider);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = TideGroupSampleTypedAuthMeansV2.getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchDataWhenSomeInformationInBankResponseAreMissing(TideGroupDataProviderV2 subject) {
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
        Map<String, BasicAuthenticationMean> configureMeans = subject.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(CLIENT_ID_NAME)).isTrue();
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }

}
