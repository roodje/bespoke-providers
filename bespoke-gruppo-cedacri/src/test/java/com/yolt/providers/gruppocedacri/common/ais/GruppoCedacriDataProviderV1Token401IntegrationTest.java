package com.yolt.providers.gruppocedacri.common.ais;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.gruppocedacri.FakeRestTemplateManager;
import com.yolt.providers.gruppocedacri.GruppoCedacriSampleTypedAuthenticationMeans;
import com.yolt.providers.gruppocedacri.GruppoCedacriTestApp;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriDataProviderV1;
import org.assertj.core.api.ThrowableAssert;
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

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test suite contains case when token endpoint return 401 error when exchangeing code for a token Gruppo Cedacri providers.
 * Tests are parametrized and run for all {@link GruppoCedacriDataProviderV1} providers in group.
 * Covered flows:
 * - creating access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = GruppoCedacriTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/gruppocedacri/ais/access-token-401", port = 0, httpsPort = 0)
@ActiveProfiles("gruppocedacri")
public class GruppoCedacriDataProviderV1Token401IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "https://yolt.com/callback";
    private static final String STATE = "d6290c4a-3ae9-415e-99cd-e572d0fca3f7";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    @Qualifier("BancaMediolanumDataProviderV1")
    private GruppoCedacriDataProviderV1 bancaMediolanumDataProviderV1;

    Stream<UrlDataProvider> getGruppoCedacriProviders() {
        return Stream.of(bancaMediolanumDataProviderV1);
    }

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void beforeEach() {
        authenticationMeans = new GruppoCedacriSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldThrowTokenInvalidExceptionWhen401WasReturned(UrlDataProvider dataProvider) {
        // given
        String redirectUrl = "https://yolt.com/callback?code=someCode&state=" + STATE;

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.createNewAccessMeans(request);
        // then
        assertThatExceptionOfType(TokenInvalidException.class).isThrownBy(throwingCallable).withMessage("We are not authorized to call endpoint: HTTP 401");
    }
}