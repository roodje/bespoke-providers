package com.yolt.providers.openbanking.ais.cybgroup.ais.v3;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupApp;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.CybgGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case when create access means method was called with wrong parameter returned from consent redirect url.
 * For such case we want to throw {@link GetAccessTokenFailedException})
 * <p>
 * Disclaimer: all providers in CYBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised,
 * so all providers in group are tested.
 * <p>
 * Covered flows:
 * - create access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CybgGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cybgroup")
class CybgGroupDataProviderV3CreateAccessMeansInvalidGrantIntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private String requestTraceId = "4bf28754-9c17-41e6-bc46-6cf98fff679";
    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> requestTraceId);

    private final CybgGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new CybgGroupSampleAuthenticationMeansV2();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("ClydesdaleDataProvider")
    private CybgGroupDataProviderV3 clydesdaleDataProvider;

    @Autowired
    @Qualifier("YorkshireDataProvider")
    private CybgGroupDataProviderV3 yorkshireDataProviderV3;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(clydesdaleDataProvider, yorkshireDataProviderV3);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getCybgGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowGetAccessTokenFailedExceptionWhenCreateNewAccessMeansWithWrongQueryParametersFromRedirectUrl(UrlDataProvider provider) {
        // given
        String redirectUrl = "https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl, userId);

        // when
        ThrowableAssert.ThrowingCallable createAccessMeansCallable = () -> provider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(createAccessMeansCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl, UUID userId) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }
}
