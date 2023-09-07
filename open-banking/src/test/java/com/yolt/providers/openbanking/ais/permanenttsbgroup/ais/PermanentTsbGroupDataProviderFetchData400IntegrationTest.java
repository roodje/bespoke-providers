package com.yolt.providers.openbanking.ais.permanenttsbgroup.ais;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupApp;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.PermanentTsbGroupBaseDataProviderV1;
import com.yolt.securityutils.signing.SignatureAlgorithm;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test suite contains case where 400 error is returned from data endpoint in Permanent TSB group providers.
 * <p>
 * Disclaimer: there is only one bank in the group, but parametrized tests can be useful, when new version of this
 * provider will be implemented. Due to that fact this test class is parametrized, so all providers in group are tested.
 * <p>
 * This means that there was a mistake in our request, thus we can't map such account (so throw {@link ProviderFetchDataException})
 * Covered flows:
 * - fetching accounts
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {PermanentTsbGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("permanenttsbgroup")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/permanenttsbgroup/ais-3.1/accounts-400"}, httpsPort = 0, port = 0)
public class PermanentTsbGroupDataProviderFetchData400IntegrationTest {

    private RestTemplateManager restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    @Qualifier("PermanentTsbDataProviderV1")
    private PermanentTsbGroupBaseDataProviderV1 permanentTsbDataProviderV1;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(permanentTsbDataProviderV1);
    }

    @Mock
    private Signer signer;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "4afee8d0-e417-4cff-b0b2-cd69f8dcc82e";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);

        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new PermanentTsbGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowProviderFetchDataExceptionWhenResponseStatusIs400(UrlDataProvider dataProvider) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest();

        // when -> then
        assertThatThrownBy(() -> dataProvider.fetchData(urlFetchData))
                .isExactlyInstanceOf(ProviderFetchDataException.class)
                .hasMessage("Failed fetching data");
    }

    private UrlFetchDataRequest createUrlFetchDataRequest() throws IOException, URISyntaxException {
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        return new UrlFetchDataRequestBuilder()
                .setAccessMeans(userId,
                        """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"7531d7a8-7f5e-4bde-ae07-a5c587458ad7","expireTime":"2021-10-19T11:55:11.525+0000","updated":"2021-07-21T11:55:11.525+0000","redirectUri":"https://www.yolt.com/callback"},"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setAuthenticationMeans(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setUserId(userId)
                .setTransactionsFetchStartTime(Instant.now().minus(90, ChronoUnit.DAYS))
                .build();
    }
}
