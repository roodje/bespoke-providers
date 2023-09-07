package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
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
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains case when create access means method was called with wrong parameter returned from consent redirect url.
 * For such case we want to throw {@link GetAccessTokenFailedException})
 * <p>
 * * Covered flows:
 * * - create access means
 * <p>
 */
@SpringBootTest(classes = {CapitalOneGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("capitalonegroup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CapitalOneDataProviderV3CreateAccessMeansInvalidGrantIntegrationTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();

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
    public void shouldThrowGetAccessTokenFailedExceptionWhenCreateNewAccessMeansWithWrongQueryParametersFromRedirectUrl(UrlDataProvider provider) {
        // given
        String redirectUrl = "https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732?error=invalid_grant";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl);

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();
    }
}
