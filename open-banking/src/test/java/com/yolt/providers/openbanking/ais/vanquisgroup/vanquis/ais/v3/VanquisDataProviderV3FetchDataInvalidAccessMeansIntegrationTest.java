package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.ais.v3;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupApp;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupSampleTypedAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.VanquisGroupBaseDataProviderV2;
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
 * This test contains case when access means are invalid (so we throw {@link GetAccessTokenFailedException})
 * <p>
 * Disclaimer: Vanquis is a single bank, so there is no need to parametrize this test class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {VanquisGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("vanquisgroupV1")
public class VanquisDataProviderV3FetchDataInvalidAccessMeansIntegrationTest {

    private static final SignerMock SIGNER = new SignerMock();

    @Autowired
    @Qualifier("VanquisDataProviderV3")
    private VanquisGroupBaseDataProviderV2 vanquisDataProvider;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new VanquisGroupSampleTypedAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenInvalidCreateAccessMeans(VanquisGroupBaseDataProviderV2 dataProvider) {
        // given
        String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setSigner(SIGNER)
                .build();

        // when -> then
        assertThatThrownBy(() -> dataProvider.createNewAccessMeans(urlCreateAccessMeans))
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    private Stream<Provider> getDataProviders() {
        return Stream.of(vanquisDataProvider);
    }
}
