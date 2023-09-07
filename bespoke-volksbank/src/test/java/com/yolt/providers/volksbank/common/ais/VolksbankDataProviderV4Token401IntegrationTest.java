package com.yolt.providers.volksbank.common.ais;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.volksbank.FakeRestTemplateManager;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.VolksbankTestApp;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test suite contains case when token endpoint return 401 error when exchangeing code for a token Volksbank group providers.
 * Tests are parametrized and run for all {@link VolksbankDataProviderV4} providers in group.
 * Covered flows:
 * - creating access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = VolksbankTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/volksbank/api_1.1/ais/access_token_401", port = 0, httpsPort = 0)
@ActiveProfiles("volksbank")
public class VolksbankDataProviderV4Token401IntegrationTest {

    private static final String LOGIN_STATE_RANDOM_UUID = "04ec2916-d42d-4240-95cf-6db789e0c4e4";
    private static final String VOLKSBANK_CONSENT_ID = "CONSENT_ID";

    @Autowired
    @Qualifier("RegioDataProviderV5")
    private VolksbankDataProviderV4 regioProviderV5;

    @Autowired
    @Qualifier("SNSDataProviderV5")
    private VolksbankDataProviderV4 snsProviderV5;

    @Autowired
    @Qualifier("ASNDataProviderV5")
    private VolksbankDataProviderV4 asnProviderV5;

    Stream<UrlDataProvider> getVolksbankProviders() {
        return Stream.of(regioProviderV5, snsProviderV5, asnProviderV5);
    }

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private List<StubMapping> stubMappings = new ArrayList<>();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @AfterEach
    public void afterEach() {
        stubMappings.forEach(WireMock::removeStub);
        stubMappings.clear();
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProviderUnderTest) {
        //given
        String redirectUrl = "https://www.yolt.com/callback/?code=SOME_AUTH_CODE&state=" + LOGIN_STATE_RANDOM_UUID;

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState(VOLKSBANK_CONSENT_ID)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProviderUnderTest.createNewAccessMeans(request);
        // then
        assertThatExceptionOfType(GetAccessTokenFailedException.class).isThrownBy(throwingCallable).withMessage("Error with status code: 401 received during call");
    }
}