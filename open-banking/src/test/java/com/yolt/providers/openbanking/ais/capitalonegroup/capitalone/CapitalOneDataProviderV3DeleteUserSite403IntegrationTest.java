package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone;

import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupApp;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains case according to documentation, when request to remove consent on bank side fails, because user
 * is unauthorized. This means that we have to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Covered flows:
 * - deleting consent on bank side
 * <p>
 */
@SpringBootTest(classes = {CapitalOneGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/capitalonegroup/ais-3.1.2/account-access-consents-delete-403", httpsPort = 0, port = 0)
@ActiveProfiles("capitalonegroup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CapitalOneDataProviderV3DeleteUserSite403IntegrationTest {

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = CapitalOneGroupSampleAuthenticationMeans.getSampleAuthenticationMeans();
    private static final RestTemplateManagerMock REST_TEMPLATE_MANAGER_MOCK = new RestTemplateManagerMock(() -> "35acdd5c-ddf1-4a70-ac0f-a4322e3bc263");

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("CapitalOneDataProviderV4")
    private CapitalOneGroupDataProviderV3 capitalOneDataProviderV4;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(capitalOneDataProviderV4);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new CapitalOneGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldDeleteUserSiteThrowTokenInvalidExceptionWhenResponseStatusIs403(UrlDataProvider provider) {
        // given
        UrlOnUserSiteDeleteRequest siteDeleteRequest = createUrlOnUserSiteDeleteRequest();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(siteDeleteRequest);

        // then
        assertThatThrownBy(onUserSiteDeleteCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Token invalid, received status 403 FORBIDDEN.");
    }

    private UrlOnUserSiteDeleteRequest createUrlOnUserSiteDeleteRequest() {
        return new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("3366f720-26a7-11e8-b65a-bd9397faa378")
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();
    }
}
