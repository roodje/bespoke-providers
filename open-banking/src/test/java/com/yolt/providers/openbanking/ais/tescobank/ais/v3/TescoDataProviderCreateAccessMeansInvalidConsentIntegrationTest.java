package com.yolt.providers.openbanking.ais.tescobank.ais.v3.v2;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
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
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("tescobank")
public class TescoDataProviderCreateAccessMeansInvalidConsentIntegrationTest {

    private static final Signer SIGNER = new SignerMock();
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();

    @Autowired
    @Qualifier("TescoBankDataProviderV7")
    private GenericBaseDataProviderV2 tescoBankDataProviderV7;

    private Stream<GenericBaseDataProviderV2> getProviders() {
        return Stream.of(tescoBankDataProviderV7);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldThrowGetAccessTokenFailedExceptionOnInvalidConsent(GenericBaseDataProviderV2 provider) throws IOException, URISyntaxException {
        // given
        String redirectUrl = "https://www.yolt.com/callback/ac75d67d-5ede-4972-94a8-3b8481fa2145?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans())
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
                .setRestTemplateManager(new RestTemplateManagerMock(() -> REQUEST_TRACE_ID))
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable handler = () -> provider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(handler)
                .isExactlyInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
    }
}