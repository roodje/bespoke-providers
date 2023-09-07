package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.ais;

import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
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
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test suite contains unhappy flow occurring in Virgin Money (Merged APIs) provider.
 * Covered flows:
 * - throw TokenInvalidException when 400 occurs on refresh token step and consent is expired
 * - throw TokenInvalidException when 400 occurs on refresh token step and there is no consent expiration time in provider state
 * - throw HttpClientErrorException when 400 occurs on refresh token step and consent is valid
 * <p>
 */
@SpringBootTest(classes = {VirginMoney2App.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney2")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/virginmoney2group/virginmoney2/oauth2/v3.0/error_400"}, httpsPort = 0, port = 0)
public class VirginMoney2DataProviderRefreshAccessMeans400IntegrationTest {

    @Autowired
    private VirginMoney2DataProviderV1 virginMoney2DataProvider;

    @Mock
    private Signer signer;

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Test
    void shouldThrowTokenInvalidExceptionWhenBadRequestOccursAndConsentIsExpired() throws IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setAccessMeans(userId, """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"eyJraWQiOiJKTzJQVVhfSGlTU3JLUlVMS01EcHNwMzRzaHciLCJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1TTJ9.eyJhdWQiOiJodHRwczpcL1wvY2TuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiMjhmNDgxYTktOTBhYy00OWExLWEyMTItZjM5ZTk5OWViZTJjIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiNjAxZjE2NjYtNTE2OC00ZTdmLTk5MzItN2MyZjI1Y2NiMDM5Iiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2IuYXBpLW5jLTN5YnNlcnZpY2VzLmNvLnVrIiwiZThwIjoxNjEzNjgwMTI0LCJpYXQiOjE2MTM2NzY1MjQsImNsaWVudF9pZCI6IjI4ZjQ4MWE5LTkwYWMtNDlhMS1hMjEyLWYzOWU5OTllYmUyYyIsImp0aSI6IjZmZGQzMDEyLTg3MjktNDA2Ni05MWFhLWQ0MzJiMTk4NDM3ZSJ9.ry6sChDHLvFGi1VPMCClTsV5PFlMiknsjwNjqK2UJjbHybks18UQGtdD79OXtz8kAooLLEqdidWsMjpH2p5tO3g9FTkIUBeWLUDm6jlhCL_ZYOTHmfcQl_rnC6rFq_OuHaK_EtkfiDWYI3LATckicQiDPoFry2u0D8sybJGGDRKCJoxw24cB4nLtvAK3FHYdKsI6IHU2klWnk986tdHs6GA_DOs-UIVQtNN9isIaTFJgsRgKitLEw2Bdolmpw0B2V2XcV5nq5qPuWGzdbhcIt4P-uwAxOZLkCW7ddvjveZV44enJ7DK-HagJT2dB59zhSQ3tPmrN9dKCvC-5kgTHwg","refreshToken":"616c0fea-5b17-49ef-8159-5db18a211a69","expireTime":"2021-10-19T11:55:11.525+0000","updated":"2021-07-21T11:55:11.525+0000","redirectUri":"http://yolt.com/redirect","consentExpirationTime":1450828801597},"permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> virginMoney2DataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(throwingCallable)
                .withMessage("Bad request occurred, but user's consent has expired");
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenBadRequestOccursAndThereIsNoInformationAboutConsentExpiryTime() throws IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setAccessMeans(userId, """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"eyJraWQiOiJKTzJQVVhfSGlTU3JLUlVMS01EcHNwMzRzaHciLCJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1TTJ9.eyJhdWQiOiJodHRwczpcL1wvY2TuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiMjhmNDgxYTktOTBhYy00OWExLWEyMTItZjM5ZTk5OWViZTJjIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiNjAxZjE2NjYtNTE2OC00ZTdmLTk5MzItN2MyZjI1Y2NiMDM5Iiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2IuYXBpLW5jLTN5YnNlcnZpY2VzLmNvLnVrIiwiZThwIjoxNjEzNjgwMTI0LCJpYXQiOjE2MTM2NzY1MjQsImNsaWVudF9pZCI6IjI4ZjQ4MWE5LTkwYWMtNDlhMS1hMjEyLWYzOWU5OTllYmUyYyIsImp0aSI6IjZmZGQzMDEyLTg3MjktNDA2Ni05MWFhLWQ0MzJiMTk4NDM3ZSJ9.ry6sChDHLvFGi1VPMCClTsV5PFlMiknsjwNjqK2UJjbHybks18UQGtdD79OXtz8kAooLLEqdidWsMjpH2p5tO3g9FTkIUBeWLUDm6jlhCL_ZYOTHmfcQl_rnC6rFq_OuHaK_EtkfiDWYI3LATckicQiDPoFry2u0D8sybJGGDRKCJoxw24cB4nLtvAK3FHYdKsI6IHU2klWnk986tdHs6GA_DOs-UIVQtNN9isIaTFJgsRgKitLEw2Bdolmpw0B2V2XcV5nq5qPuWGzdbhcIt4P-uwAxOZLkCW7ddvjveZV44enJ7DK-HagJT2dB59zhSQ3tPmrN9dKCvC-5kgTHwg","refreshToken":"616c0fea-5b17-49ef-8159-5db18a211a69","expireTime":"2021-10-19T11:55:11.525+0000","updated":"2021-07-21T11:55:11.525+0000","redirectUri":"http://yolt.com/redirect"},"permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> virginMoney2DataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(throwingCallable)
                .withMessage("Bad request occurred, but user's consent has expired");
    }

    @Test
    void shouldThrowHttpStatusCodeExceptionWhenBadRequestOccursAndConsentIsValid() throws IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        long consentExpirationTime = Instant.now().plusSeconds(360000L).toEpochMilli();
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setAccessMeans(userId, """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"eyJraWQiOiJKTzJQVVhfSGlTU3JLUlVMS01EcHNwMzRzaHciLCJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1TTJ9.eyJhdWQiOiJodHRwczpcL1wvY2TuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiMjhmNDgxYTktOTBhYy00OWExLWEyMTItZjM5ZTk5OWViZTJjIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiNjAxZjE2NjYtNTE2OC00ZTdmLTk5MzItN2MyZjI1Y2NiMDM5Iiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2IuYXBpLW5jLTN5YnNlcnZpY2VzLmNvLnVrIiwiZThwIjoxNjEzNjgwMTI0LCJpYXQiOjE2MTM2NzY1MjQsImNsaWVudF9pZCI6IjI4ZjQ4MWE5LTkwYWMtNDlhMS1hMjEyLWYzOWU5OTllYmUyYyIsImp0aSI6IjZmZGQzMDEyLTg3MjktNDA2Ni05MWFhLWQ0MzJiMTk4NDM3ZSJ9.ry6sChDHLvFGi1VPMCClTsV5PFlMiknsjwNjqK2UJjbHybks18UQGtdD79OXtz8kAooLLEqdidWsMjpH2p5tO3g9FTkIUBeWLUDm6jlhCL_ZYOTHmfcQl_rnC6rFq_OuHaK_EtkfiDWYI3LATckicQiDPoFry2u0D8sybJGGDRKCJoxw24cB4nLtvAK3FHYdKsI6IHU2klWnk986tdHs6GA_DOs-UIVQtNN9isIaTFJgsRgKitLEw2Bdolmpw0B2V2XcV5nq5qPuWGzdbhcIt4P-uwAxOZLkCW7ddvjveZV44enJ7DK-HagJT2dB59zhSQ3tPmrN9dKCvC-5kgTHwg","refreshToken":"616c0fea-5b17-49ef-8159-5db18a211a69","expireTime":"2021-10-19T11:55:11.525+0000","updated":"2021-07-21T11:55:11.525+0000","redirectUri":"http://yolt.com/redirect","consentExpirationTime":"""
                                + consentExpirationTime + """
                                },"permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> virginMoney2DataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(throwingCallable)
                .withMessage("400 Bad Request: [Body length: 190, Check RDD to see content of body]");
    }
}
