package com.yolt.providers.openbanking.ais.barclaysgroup.ais;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case when redirect url contains information about invalid consent
 * This means that access token can't be created, thus we want to inform user that this step failed (so throw {@link GetAccessTokenFailedException})
 * <p>
 * Covered flows:
 * - creating access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("barclays")
public class BarclaysDataProviderCreateAccessMeansInvalidConsentIntegrationTest {

    private static final Signer SIGNER = new SignerMock();

    private String requestTraceId = "d0a9b85f-9715-4d16-a33d-4323ceab5254";

    @Autowired
    @Qualifier("BarclaysDataProviderV16")
    private GenericBaseDataProviderV2 barclaysDataProviderV16;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(barclaysDataProviderV16);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowGetAccessTokenFailedExceptionOnInvalidConsent(UrlDataProvider subject) throws IOException, URISyntaxException {
        // given
        String redirectUrl = "https://www.yolt.com/callback/ac75d67d-5ede-4972-94a8-3b8481fa2145?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
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
                .setAuthenticationMeans(new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> requestTraceId))
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable handler = () -> subject.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(handler).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}