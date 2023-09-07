package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.VirginMoney2DataProviderV1;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.auth.VirginMoney2GroupAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2App;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2JwsSigningResult;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2SampleAuthenticationMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * This test suite contains all happy flows occurring in Virgin Money (Merged APIs) provider.
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts, balances, transactions
 * <p>
 */
@SpringBootTest(classes = {VirginMoney2App.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney2")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/virginmoney2group/virginmoney2/registration/v3.2/happy-flow",
        "classpath:/stubs/virginmoney2group/virginmoney2/oauth2/v3.0/happy-flow",
        "classpath:/stubs/virginmoney2group/virginmoney2/ais/v3.1.2/happy-flow"},
        httpsPort = 0,
        port = 0)
public class VirginMoney2DataProviderHappyFlowIntegrationTest {

    @Autowired
    private VirginMoney2DataProviderV1 virginMoney2DataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @Mock
    private Signer signer;

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Test
    void shouldReturnClientIdAndClientSecretTypedAuthMeansForGetAutoConfiguredMeans() {
        // when
        Map<String, TypedAuthenticationMeans> result = virginMoney2DataProvider.getAutoConfiguredMeans();

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                VirginMoney2GroupAuthMeansBuilder.CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING,
                VirginMoney2GroupAuthMeansBuilder.CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING
        ));
    }

    @Test
    void shouldReturnAuthenticationMeansWithClientIdAndClientSecretForAutoConfigureMeansWhenCorrectDataAreProvided() throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAutoonboarding();
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setRedirectUrls(List.of("http://redirect1", "http://redirect2"))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS))
                .build();

        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(new VirginMoney2JwsSigningResult());

        // when
        Map<String, BasicAuthenticationMean> result = virginMoney2DataProvider.autoConfigureMeans(autoOnboardingRequest);

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis());
    }

    @Test
    void shouldReturnRedirectStepWithConsentIdProperRedirectUrlAndProviderStateForGetLoginInfoWhenCorrectDataAreProvided() throws IOException, URISyntaxException {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setState("state")
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("http://yolt.com/redirect")
                .build();

        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(new VirginMoney2JwsSigningResult());

        // when
        RedirectStep result = (RedirectStep) virginMoney2DataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(result.getExternalConsentId()).isEqualTo("someConsentId");
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                OAuth.RESPONSE_TYPE, "code+id_token",
                OAuth.CLIENT_ID, "someClientId",
                OAuth.STATE, "state",
                OAuth.SCOPE, "openid+accounts",
                OAuth.NONCE, "state",
                OAuth.REDIRECT_URI, "http%3A%2F%2Fyolt.com%2Fredirect",
                "request", "V2hhdCBoYXRoIGdvZCB3cm91Z2h0ID8%3D..QnkgR2VvcmdlLCBzaGUncyBnb3QgaXQhIEJ5IEdlb3JnZSBzaGUncyBnb3QgaXQhIE5vdyBvbmNlIGFnYWluLCB3aGVyZSBkb2VzIGl0IHJhaW4"
        ));
        assertThat(result.getProviderState()).isEqualTo("""
                {"permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]}""");
    }

    @Test
    void shouldReturnAccessMeansForCreateNewAccessMeansWhenCorrectDataAreProvided() throws IOException, URISyntaxException {
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
        AccessMeansOrStepDTO result = virginMoney2DataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThat(result.getStep()).isNull();
        assertThat(result.getAccessMeans()).satisfies(accessMeansDTO -> {
            assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "accessToken":"eyJraWQiOiJKTzJQVVhfSGlTU3JLUlVMS01EcHNwMzRzaHciLCJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1TTJ9.eyJhdWQiOiJodHRwczpcL1wvY2TuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiMjhmNDgxYTktOTBhYy00OWExLWEyMTItZjM5ZTk5OWViZTJjIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiNjAxZjE2NjYtNTE2OC00ZTdmLTk5MzItN2MyZjI1Y2NiMDM5Iiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2IuYXBpLW5jLTN5YnNlcnZpY2VzLmNvLnVrIiwiZThwIjoxNjEzNjgwMTI0LCJpYXQiOjE2MTM2NzY1MjQsImNsaWVudF9pZCI6IjI4ZjQ4MWE5LTkwYWMtNDlhMS1hMjEyLWYzOWU5OTllYmUyYyIsImp0aSI6IjZmZGQzMDEyLTg3MjktNDA2Ni05MWFhLWQ0MzJiMTk4NDM3ZSJ9.ry6sChDHLvFGi1VPMCClTsV5PFlMiknsjwNjqK2UJjbHybks18UQGtdD79OXtz8kAooLLEqdidWsMjpH2p5tO3g9FTkIUBeWLUDm6jlhCL_ZYOTHmfcQl_rnC6rFq_OuHaK_EtkfiDWYI3LATckicQiDPoFry2u0D8sybJGGDRKCJoxw24cB4nLtvAK3FHYdKsI6IHU2klWnk986tdHs6GA_DOs-UIVQtNN9isIaTFJgsRgKitLEw2Bdolmpw0B2V2XcV5nq5qPuWGzdbhcIt4P-uwAxOZLkCW7ddvjveZV44enJ7DK-HagJT2dB59zhSQ3tPmrN9dKCvC-5kgTHwg""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "refreshToken":"616c0fea-5b17-49ef-8159-5db18a211a69""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "redirectUri":"http://yolt.com/redirect""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "consentExpirationTime":""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]""");
            assertThat(accessMeansDTO.getExpireTime()).isCloseTo(new Date(), 7776000000L);
            assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), 5000);
        });
    }

    @Test
    void shouldReturnRenewedAccessMeansForRefreshAccessMeansWhenCorrectDataAreProvided() throws TokenInvalidException, IOException, URISyntaxException {
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
        AccessMeansDTO result = virginMoney2DataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAccessMeans()).contains("""
                "userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47""");
        assertThat(result.getAccessMeans()).contains("""
                "accessToken":"eyJraWQiOiJfNVgyQT92RGE3NWdqRVZDVjM2OFBudnU4NGsiLCJ0eXAiOiJKV1QiLCJhbGciOiJQTzI1NiJ9.eyJhdWQiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiOGJiZWFkMjUtZmY0MS00OGIwLTk4NDQtZTE0NzEzNGJmODRkIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiMTNmM2JiYjktOWNiMC00YmQzLWJlMzctY2VlMWM0OWUzYjAxIiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwiZXhwIjoxNjEzNjY3NjE0LCJpYXQiOjE2MTM2NjQwMTQsImNsaWVudF9pZCI6IjhiYmVhZDI1LWZmNDEtNDhiMC05TDQ0LWYxNDcxMzRiZjg0ZCIsImp0aSI6IjI2NmU3Yjc0LWFhZDEtNGFjMC1iNGEzLTc3NjJlMTdlYzViMCJ9.gDT_MWnvXWm9uOlPxi_QntqLPhfpNyf9Wd8KXNjiF80pYicCn1IGx96Y-RauuZGpAyNylKpZESTe_RQXy1ZsCGZ-OJsB5JcArt0BuDAm1UFcCbXwoeQAqyVtMgASAd1B1r4edCJYbA9X_cBUOWQTZcgCfdqWYePrGuYyPlSzKC1zhjav6N4uWgb0ewmn5Zu_Q2RNSS9E-79V5crwzIUVS6YVzZyazhFBzdfpDLweiQtgk9YawYkTE8Sr1RPg8z49GUHn9G30RgaTGQGQ55gxXz9KSmb3RsLvx99XM2Bm6JZCSVUSpTmAmAY_viEfrg_bakyTvlsw-NjzsTOt3EXp-Q""");
        assertThat(result.getAccessMeans()).contains("""
                "refreshToken":"43ee9a48-b1f9-4f41-8ccc-9f1aa60e71b5""");
        assertThat(result.getAccessMeans()).contains("""
                "redirectUri":"http://yolt.com/redirect""");
        assertThat(result.getAccessMeans()).contains("""
                "consentExpirationTime":""" + consentExpirationTime);
        assertThat(result.getAccessMeans()).contains("""
                "permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]""");
        assertThat(result.getExpireTime()).isCloseTo(new Date(), 7776000000L);
        assertThat(result.getUpdated()).isCloseTo(new Date(), 5000);
    }

    @Test
    void shouldReturnRenewedAccessMeansForRefreshAccessMeansWithTheSameRefreshTokenWhenCorrectDataAreProvided() throws TokenInvalidException, IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        long consentExpirationTime = Instant.now().plusSeconds(360000L).toEpochMilli();
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setAccessMeans(userId, """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"eyJraWQiOiJKTzJQVVhfSGlTU3JLUlVMS01EcHNwMzRzaHciLCJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1TTJ9.eyJhdWQiOiJodHRwczpcL1wvY2TuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiMjhmNDgxYTktOTBhYy00OWExLWEyMTItZjM5ZTk5OWViZTJjIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiNjAxZjE2NjYtNTE2OC00ZTdmLTk5MzItN2MyZjI1Y2NiMDM5Iiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2IuYXBpLW5jLTN5YnNlcnZpY2VzLmNvLnVrIiwiZThwIjoxNjEzNjgwMTI0LCJpYXQiOjE2MTM2NzY1MjQsImNsaWVudF9pZCI6IjI4ZjQ4MWE5LTkwYWMtNDlhMS1hMjEyLWYzOWU5OTllYmUyYyIsImp0aSI6IjZmZGQzMDEyLTg3MjktNDA2Ni05MWFhLWQ0MzJiMTk4NDM3ZSJ9.ry6sChDHLvFGi1VPMCClTsV5PFlMiknsjwNjqK2UJjbHybks18UQGtdD79OXtz8kAooLLEqdidWsMjpH2p5tO3g9FTkIUBeWLUDm6jlhCL_ZYOTHmfcQl_rnC6rFq_OuHaK_EtkfiDWYI3LATckicQiDPoFry2u0D8sybJGGDRKCJoxw24cB4nLtvAK3FHYdKsI6IHU2klWnk986tdHs6GA_DOs-UIVQtNN9isIaTFJgsRgKitLEw2Bdolmpw0B2V2XcV5nq5qPuWGzdbhcIt4P-uwAxOZLkCW7ddvjveZV44enJ7DK-HagJT2dB59zhSQ3tPmrN9dKCvC-5kgTHwg","refreshToken":"THE-REFRESH-TOKEN-WHICH-SHOULD-STAY-THE-SAME","expireTime":"2021-10-19T11:55:11.525+0000","updated":"2021-07-21T11:55:11.525+0000","redirectUri":"http://yolt.com/redirect","consentExpirationTime":"""
                                + consentExpirationTime + """
                                },"permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO result = virginMoney2DataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAccessMeans()).contains("""
                "userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47""");
        assertThat(result.getAccessMeans()).contains("""
                "accessToken":"eyJraWQiOiJfNVgyQT92RGE3NWdqRVZDVjM2OFBudnU4NGsiLCJ0eXAiOiJKV1QiLCJhbGciOiJQTzI1NiJ9.eyJhdWQiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiOGJiZWFkMjUtZmY0MS00OGIwLTk4NDQtZTE0NzEzNGJmODRkIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiMTNmM2JiYjktOWNiMC00YmQzLWJlMzctY2VlMWM0OWUzYjAxIiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwiZXhwIjoxNjEzNjY3NjE0LCJpYXQiOjE2MTM2NjQwMTQsImNsaWVudF9pZCI6IjhiYmVhZDI1LWZmNDEtNDhiMC05TDQ0LWYxNDcxMzRiZjg0ZCIsImp0aSI6IjI2NmU3Yjc0LWFhZDEtNGFjMC1iNGEzLTc3NjJlMTdlYzViMCJ9.gDT_MWnvXWm9uOlPxi_QntqLPhfpNyf9Wd8KXNjiF80pYicCn1IGx96Y-RauuZGpAyNylKpZESTe_RQXy1ZsCGZ-OJsB5JcArt0BuDAm1UFcCbXwoeQAqyVtMgASAd1B1r4edCJYbA9X_cBUOWQTZcgCfdqWYePrGuYyPlSzKC1zhjav6N4uWgb0ewmn5Zu_Q2RNSS9E-79V5crwzIUVS6YVzZyazhFBzdfpDLweiQtgk9YawYkTE8Sr1RPg8z49GUHn9G30RgaTGQGQ55gxXz9KSmb3RsLvx99XM2Bm6JZCSVUSpTmAmAY_viEfrg_bakyTvlsw-NjzsTOt3EXp-Q""");
        assertThat(result.getAccessMeans()).contains("""
                "refreshToken":"THE-REFRESH-TOKEN-WHICH-SHOULD-STAY-THE-SAME""");
        assertThat(result.getAccessMeans()).contains("""
                "redirectUri":"http://yolt.com/redirect""");
        assertThat(result.getAccessMeans()).contains("""
                "consentExpirationTime":""" + consentExpirationTime);
        assertThat(result.getAccessMeans()).contains("""
                "permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]""");
        assertThat(result.getExpireTime()).isCloseTo(new Date(), 7776000000L);
        assertThat(result.getUpdated()).isCloseTo(new Date(), 5000);
    }

    @Test
    void shouldReturnRenewedAccessMeansForRefreshAccessMeansWhenConsentExpirationTimeIsMissing() throws TokenInvalidException, IOException, URISyntaxException {
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
        AccessMeansDTO result = virginMoney2DataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAccessMeans()).contains("""
                "userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47""");
        assertThat(result.getAccessMeans()).contains("""
                "accessToken":"eyJraWQiOiJfNVgyQT92RGE3NWdqRVZDVjM2OFBudnU4NGsiLCJ0eXAiOiJKV1QiLCJhbGciOiJQTzI1NiJ9.eyJhdWQiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiOGJiZWFkMjUtZmY0MS00OGIwLTk4NDQtZTE0NzEzNGJmODRkIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiMTNmM2JiYjktOWNiMC00YmQzLWJlMzctY2VlMWM0OWUzYjAxIiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwiZXhwIjoxNjEzNjY3NjE0LCJpYXQiOjE2MTM2NjQwMTQsImNsaWVudF9pZCI6IjhiYmVhZDI1LWZmNDEtNDhiMC05TDQ0LWYxNDcxMzRiZjg0ZCIsImp0aSI6IjI2NmU3Yjc0LWFhZDEtNGFjMC1iNGEzLTc3NjJlMTdlYzViMCJ9.gDT_MWnvXWm9uOlPxi_QntqLPhfpNyf9Wd8KXNjiF80pYicCn1IGx96Y-RauuZGpAyNylKpZESTe_RQXy1ZsCGZ-OJsB5JcArt0BuDAm1UFcCbXwoeQAqyVtMgASAd1B1r4edCJYbA9X_cBUOWQTZcgCfdqWYePrGuYyPlSzKC1zhjav6N4uWgb0ewmn5Zu_Q2RNSS9E-79V5crwzIUVS6YVzZyazhFBzdfpDLweiQtgk9YawYkTE8Sr1RPg8z49GUHn9G30RgaTGQGQ55gxXz9KSmb3RsLvx99XM2Bm6JZCSVUSpTmAmAY_viEfrg_bakyTvlsw-NjzsTOt3EXp-Q""");
        assertThat(result.getAccessMeans()).contains("""
                "refreshToken":"43ee9a48-b1f9-4f41-8ccc-9f1aa60e71b5""");
        assertThat(result.getAccessMeans()).contains("""
                "redirectUri":"http://yolt.com/redirect""");
        assertThat(result.getAccessMeans()).contains("""
                "consentExpirationTime":0""");
        assertThat(result.getAccessMeans()).contains("""
                "permissions":["ReadParty","ReadAccountsDetail","ReadBalances","ReadDirectDebits","ReadProducts","ReadStandingOrdersDetail","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"]""");
        assertThat(result.getExpireTime()).isCloseTo(new Date(), 7776000000L);
        assertThat(result.getUpdated()).isCloseTo(new Date(), 5000);
    }

    @Test
    void shouldReturnDataProviderResponseWithAllDataProperlyMappedForFetchDataWhenCorrectDataAreProvided() throws IOException, URISyntaxException, TokenInvalidException, ProviderFetchDataException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        Instant now = Instant.now();
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(userId, objectMapper.writeValueAsString(new AccessMeansState(new AccessMeans(
                                now.minus(1, ChronoUnit.MINUTES),
                                userId,
                                "eyJraWQiOiJKTzJQVVhfSGlTU3JLUlVMS01EcHNwMzRzaHciLCJ0eXAiOiJKV1QiLCJhbGciOiJQUzI1TTJ9.eyJhdWQiOiJodHRwczpcL1wvY2TuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiMjhmNDgxYTktOTBhYy00OWExLWEyMTItZjM5ZTk5OWViZTJjIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiNjAxZjE2NjYtNTE2OC00ZTdmLTk5MzItN2MyZjI1Y2NiMDM5Iiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wvY2IuYXBpLW5jLTN5YnNlcnZpY2VzLmNvLnVrIiwiZThwIjoxNjEzNjgwMTI0LCJpYXQiOjE2MTM2NzY1MjQsImNsaWVudF9pZCI6IjI4ZjQ4MWE5LTkwYWMtNDlhMS1hMjEyLWYzOWU5OTllYmUyYyIsImp0aSI6IjZmZGQzMDEyLTg3MjktNDA2Ni05MWFhLWQ0MzJiMTk4NDM3ZSJ9.ry6sChDHLvFGi1VPMCClTsV5PFlMiknsjwNjqK2UJjbHybks18UQGtdD79OXtz8kAooLLEqdidWsMjpH2p5tO3g9FTkIUBeWLUDm6jlhCL_ZYOTHmfcQl_rnC6rFq_OuHaK_EtkfiDWYI3LATckicQiDPoFry2u0D8sybJGGDRKCJoxw24cB4nLtvAK3FHYdKsI6IHU2klWnk986tdHs6GA_DOs-UIVQtNN9isIaTFJgsRgKitLEw2Bdolmpw0B2V2XcV5nq5qPuWGzdbhcIt4P-uwAxOZLkCW7ddvjveZV44enJ7DK-HagJT2dB59zhSQ3tPmrN9dKCvC-5kgTHwg",
                                "616c0fea-5b17-49ef-8159-5db18a211a69",
                                new Date(now.plus(365, ChronoUnit.DAYS).toEpochMilli()),
                                new Date(now.toEpochMilli()),
                                "http://yolt.com/redirect"
                        ), List.of("ReadParty",
                                "ReadAccountsDetail",
                                "ReadBalances",
                                "ReadDirectDebits",
                                "ReadProducts",
                                "ReadStandingOrdersDetail",
                                "ReadTransactionsCredits",
                                "ReadTransactionsDebits",
                                "ReadTransactionsDetail"))),
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setAuthenticationMeans(new VirginMoney2SampleAuthenticationMeans().getVirginMoney2SampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setUserId(userId)
                .setTransactionsFetchStartTime(Instant.now().minus(90, ChronoUnit.DAYS))
                .build();

        // when
        DataProviderResponse result = virginMoney2DataProvider.fetchData(urlFetchDataRequest);

        // then
        assertThat(result.getAccounts()).hasSize(2);
        assertThat(findAccount(result.getAccounts(), "66c39bb1-74f9-454c-a0df-9996bb50560a")).satisfies(currentAccountAssertions());
        assertThat(findAccount(result.getAccounts(), "7cda0cfb-b99e-466a-8a1c-6dd5954ce9b9")).satisfies(savingsAccountAssertions());
    }

    private ProviderAccountDTO findAccount(List<ProviderAccountDTO> accounts, String accountId) {
        return accounts.stream().filter(acc -> acc.getAccountId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    private Consumer<ProviderAccountDTO> currentAccountAssertions() {
        return currentAccount -> {
            assertThat(currentAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
            assertThat(currentAccount.getCurrentBalance()).isEqualTo("1154.00");
            assertThat(currentAccount.getAvailableBalance()).isEqualTo("2904.72");
            assertThat(currentAccount.getAccountId()).isEqualTo("66c39bb1-74f9-454c-a0df-9996bb50560a");
            assertThat(currentAccount.getAccountNumber()).satisfies(accountNumber -> {
                assertThat(accountNumber.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
                assertThat(accountNumber.getIdentification()).isEqualTo("98765432109876");
                assertThat(accountNumber.getHolderName()).isEqualTo("John Doe");
            });
            assertThat(currentAccount.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), byLessThan(1, ChronoUnit.MINUTES));
            assertThat(currentAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(currentAccount.getName()).isEqualTo("66c39bb1-74f9-454c-a0df-9996bb50560a");
            assertThat(currentAccount.getTransactions()).first().satisfies(firstTransactionAssertions());
            assertThat(currentAccount.getDirectDebits()).first().satisfies(firstDirectDebitAssertions());
            assertThat(currentAccount.getStandingOrders()).first().satisfies(firstStandingOrderAssertions());
            assertThat(currentAccount.getExtendedAccount()).satisfies(extendedAccountAssertions());
        };
    }

    private Consumer<ProviderAccountDTO> savingsAccountAssertions() {
        return savingsAccount -> {
            assertThat(savingsAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
            assertThat(savingsAccount.getCurrentBalance()).isEqualTo("1001.33");
            assertThat(savingsAccount.getAvailableBalance()).isEqualTo("1001.33");
            assertThat(savingsAccount.getAccountId()).isEqualTo("7cda0cfb-b99e-466a-8a1c-6dd5954ce9b9");
            assertThat(savingsAccount.getAccountNumber()).satisfies(accountNumber -> {
                assertThat(accountNumber.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
                assertThat(accountNumber.getIdentification()).isEqualTo("GB15AIBK12345678901235");
                assertThat(accountNumber.getHolderName()).isEqualTo("John Doe");
            });
            assertThat(savingsAccount.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), byLessThan(1, ChronoUnit.MINUTES));
            assertThat(savingsAccount.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
            assertThat(savingsAccount.getName()).isEqualTo("Mr Robin Hood");
        };
    }

    private Consumer<ProviderTransactionDTO> firstTransactionAssertions() {
        return transaction -> {
            assertThat(transaction.getAmount()).isEqualTo("4.72");
            assertThat(transaction.getDescription()).isEqualTo("INTEREST EARNED");
            assertThat(transaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
            assertThat(transaction.getDateTime()).isEqualTo("2020-11-10T00:00:00+01:00");
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
            assertThat(transaction.getExtendedTransaction()).satisfies(extendedTransactionAssertions());
        };
    }

    private Consumer<ExtendedTransactionDTO> extendedTransactionAssertions() {
        return extendedTransaction -> {
            assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-10T00:00:00+01:00");
            assertThat(extendedTransaction.getTransactionAmount()).satisfies(amount -> {
                assertThat(amount.getCurrency()).isEqualTo(CurrencyCode.GBP);
                assertThat(amount.getAmount()).isEqualTo("4.72");
            });
            assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(extendedTransaction.getProprietaryBankTransactionCode()).isEqualTo("Interest");
            assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("INTEREST EARNED");
            assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-10T00:00:00+01:00");
        };
    }

    private Consumer<DirectDebitDTO> firstDirectDebitAssertions() {
        return directDebit -> {
            assertThat(directDebit.getDescription()).isEqualTo("Sherwood Bow Company");
            assertThat(directDebit.getPreviousPaymentAmount()).isEqualTo("6.57");
            assertThat(directDebit.getPreviousPaymentDateTime()).isEqualTo("2018-12-05T10:52:43.123+00:00");
        };
    }

    private Consumer<StandingOrderDTO> firstStandingOrderAssertions() {
        return standingOrder -> {
            assertThat(standingOrder.getStandingOrderId()).isEqualTo("27812801");
            assertThat(standingOrder.getDescription()).isEqualTo("Robin Hood");
            assertThat(standingOrder.getCounterParty()).satisfies(counterParty -> {
                assertThat(counterParty.getHolderName()).isEqualTo("Maid Marian");
                assertThat(counterParty.getIdentification()).isEqualTo("50506492837451");
                assertThat(counterParty.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
            });
            assertThat(standingOrder.getFrequency()).isEqualTo(Period.ofMonths(1));
            assertThat(standingOrder.getNextPaymentAmount()).isEqualTo("150.00");
            assertThat(standingOrder.getNextPaymentDateTime()).isEqualTo("2021-08-05T06:29:11.454+00:00");
        };
    }

    private Consumer<ExtendedAccountDTO> extendedAccountAssertions() {
        return extendedAccount -> {
            assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
            assertThat(extendedAccount.getName()).isEqualTo("66c39bb1-74f9-454c-a0df-9996bb50560a");
            assertThat(extendedAccount.getStatus()).isEqualTo(Status.ENABLED);
            assertThat(findBalance(extendedAccount.getBalances(), BalanceType.INTERIM_BOOKED)).satisfies(balance -> {
                assertThat(balance.getBalanceAmount()).satisfies(amount -> {
                    assertThat(amount.getAmount()).isEqualTo("1154.00");
                    assertThat(amount.getCurrency()).isEqualTo(CurrencyCode.GBP);
                });
                assertThat(balance.getBalanceType()).isEqualTo(BalanceType.INTERIM_BOOKED);
                assertThat(balance.getLastChangeDateTime()).isEqualTo("2020-11-19T13:36:39.656Z");
                assertThat(balance.getReferenceDate()).isEqualTo("2020-11-19T13:36:39.656Z");
            });
            assertThat(findBalance(extendedAccount.getBalances(), BalanceType.INTERIM_AVAILABLE)).satisfies(balance -> {
                assertThat(balance.getBalanceAmount()).satisfies(amount -> {
                    assertThat(amount.getAmount()).isEqualTo("2904.72");
                    assertThat(amount.getCurrency()).isEqualTo(CurrencyCode.GBP);
                });
                assertThat(balance.getBalanceType()).isEqualTo(BalanceType.INTERIM_AVAILABLE);
                assertThat(balance.getLastChangeDateTime()).isEqualTo("2020-11-19T13:36:39.656Z");
                assertThat(balance.getReferenceDate()).isEqualTo("2020-11-19T13:36:39.656Z");
            });
            assertThat(extendedAccount.getBic()).isEqualTo("NRNBGB8VLBQ");
            assertThat(extendedAccount.getResourceId()).isEqualTo("66c39bb1-74f9-454c-a0df-9996bb50560a");
            assertThat(extendedAccount.getUsage()).isEqualTo(UsageType.PRIVATE);
        };
    }

    private BalanceDTO findBalance(List<BalanceDTO> balances, BalanceType type) {
        return balances.stream().filter(balance -> balance.getBalanceType() == type)
                .findFirst()
                .orElse(null);
    }
}
