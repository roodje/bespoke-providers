package com.yolt.providers.openbanking.ais.aibgroup;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AibGroupDataProviderRedirectUrlWithInvalidGrantErrorIntegrationTest {

    private static final Signer SIGNER = new SignerMock();
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> UUID.randomUUID().toString());

    private GenericBaseDataProvider aibDataProvider = new GenericBaseDataProvider(null, null, null, null, null, new ProviderIdentification("AIB", null, null), null, null, null, null, null, null);
    private GenericBaseDataProvider aibNIDataProvider = new GenericBaseDataProvider(null, null, null, null, null, new ProviderIdentification("FIRST_DIRECT", null, null), null, null, null, null, null, null);
    private GenericBaseDataProvider aibIeDataProvider = new GenericBaseDataProvider(null, null, null, null, null, new ProviderIdentification("AIB_IE", null, null), null, null, null, null, null, null);

    private Stream<GenericBaseDataProvider> getAibProviders() {
        return Stream.of(aibDataProvider, aibNIDataProvider, aibIeDataProvider);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = AibGroupSampleAuthenticationMeans.getAibGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldResultInStatusReasonTokenExpiredAndLogsRedirectUrlWithErrorInQueryParametersForCreateNewAccessMeans(GenericBaseDataProvider aibDataProvider) {
        // given
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545?error=invalid_grant";
        UrlCreateAccessMeansRequest urlCreateAccessMeans =
                new UrlCreateAccessMeansRequestBuilder()
                        .setRestTemplateManager(restTemplateManager)
                        .setRedirectUrlPostedBackFromSite(redirectUrl)
                        .setAuthenticationMeans(authenticationMeans)
                        .setSigner(SIGNER)
                        .build();

        // when
        assertThatThrownBy(() -> aibDataProvider.createNewAccessMeans(urlCreateAccessMeans))
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}
