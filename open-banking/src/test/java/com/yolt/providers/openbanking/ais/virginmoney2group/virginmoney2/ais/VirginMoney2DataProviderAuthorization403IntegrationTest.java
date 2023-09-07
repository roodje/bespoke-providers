package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.ais;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.VirginMoney2DataProviderV1;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2App;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2SampleAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test suite contains unhappy flow occurring in Virgin Money (Merged APIs) provider.
 * Covered flows:
 * - 403 when creating access means
 * <p>
 */
@SpringBootTest(classes = {VirginMoney2App.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney2")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/virginmoney2group/virginmoney2/oauth2/v3.0/error_403"},
        httpsPort = 0,
        port = 0)
public class VirginMoney2DataProviderAuthorization403IntegrationTest {

    @Autowired
    private VirginMoney2DataProviderV1 virginMoney2DataProvider;

    @Mock
    private Signer signer;

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Test
    void shouldThrowGetAccessTokenFailedExceptionWhen403ResponseFromAuthorizationEndpointIsReceived() throws IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setState("state")
                .setProviderState("""
                        {"permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]}""")
                .setRedirectUrlPostedBackFromSite("http://yolt.com/redirect?code=bd941a87-116c-46b5-915e-47ea05711734")
                .setUserId(userId)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> virginMoney2DataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThatExceptionOfType(GetAccessTokenFailedException.class)
                .isThrownBy(throwingCallable)
                .withMessage("Token invalid, received status 403 FORBIDDEN.");
    }
}
