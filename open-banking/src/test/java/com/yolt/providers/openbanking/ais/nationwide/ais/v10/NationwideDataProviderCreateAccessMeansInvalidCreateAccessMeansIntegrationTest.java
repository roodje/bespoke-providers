package com.yolt.providers.openbanking.ais.nationwide.ais.v10;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideDataProviderV10;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case when redirect url contains information about invalid consent
 * This means that access token can't be created, thus we want to inform user that this step failed (so throw {@link GetAccessTokenFailedException})
 * Covered flows:
 * - creating access means
 */
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("nationwide")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NationwideDataProviderCreateAccessMeansInvalidCreateAccessMeansIntegrationTest {

    @Autowired
    @Qualifier("NationwideDataProviderV11")
    private NationwideDataProviderV10 nationwideDataProviderV11;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(nationwideDataProviderV11);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenInvalidCreateAccessMeans(UrlDataProvider provider) throws IOException, URISyntaxException {
        // given
        String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(new NationwideSampleAuthenticationMeans().getAuthenticationMeans())
                .setProviderState("""
                        {"permissions":["ReadParty",\
                        "ReadAccountsDetail",\
                        "ReadBalances",\
                        "ReadDirectDebits",\
                        "ReadProducts",\
                        "ReadStandingOrdersDetail",\
                        "ReadTransactionsCredits",\
                        "ReadTransactionsDebits",\
                        "ReadTransactionsDetail"]}\
                        """)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}