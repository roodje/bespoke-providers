package com.yolt.providers.amexgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.yolt.providers.amexgroup.common.AmexGroupDataProviderV6;
import com.yolt.providers.amexgroup.common.dto.TokenResponse;
import com.yolt.providers.amexgroup.common.dto.TokenResponses;
import com.yolt.providers.amexgroup.common.utils.AmexDateTimeUtils;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.yolt.providers.amexgroup.common.utils.AmexAuthMeansFields.*;
import static nl.ing.lovebird.extendeddata.account.BalanceType.AVAILABLE;
import static nl.ing.lovebird.extendeddata.account.Status.ENABLED;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.*;
import static nl.ing.lovebird.extendeddata.transaction.AccountReferenceType.MASKED_PAN;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/", httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
public class AmexGroupDataProviderIntegrationTest {

    private static final String REDIRECT_URL = "https://localhost/redirect/auth";
    private static final String CODE = "THE-CODE";
    private static final String CODE_NEW = "THE-NEW-CODE";
    private static final String WRONG_CODE = "THE-WRONG-CODE";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TOKEN = "the-access-token";
    private static final String REFRESH_TOKEN = "the-refresh-token";
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = prepareMeans();

    private Signer signer = mock(Signer.class);

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    @Qualifier("AmexGroupObjectMapper")
    private ObjectMapper objectMapper;

    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("AmexDataProviderV6")
    private AmexGroupDataProviderV6 amexDataProviderV6;

    @Autowired
    @Qualifier("AmexEuDataProviderV6")
    private AmexGroupDataProviderV6 amexEuDataProviderV6;

    private Stream<UrlDataProvider> getAmexGroupDataProviders() {
        return Stream.of(amexDataProviderV6, amexEuDataProviderV6);
    }

    private List<StubMapping> stubMappings = new ArrayList<>();

    @BeforeAll
    public void beforeEach() {
        externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory()
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setOutputStreaming(false);
                    return factory;
                });

        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory,
                AUTH_MEANS.get(CLIENT_TRANSPORT_KEY_ID_ROTATION).getValue());
    }

    @AfterEach
    public void afterEach() {
        stubMappings.forEach(WireMock::removeStub);
        stubMappings.clear();
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldGetTypedAuthenticationMeans(UrlDataProvider provider) {
        // given
        final Map<String, TypedAuthenticationMeans> expectedTypedAuthenticationMeans = new HashMap<>();
        expectedTypedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        expectedTypedAuthenticationMeans.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        expectedTypedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, TypedAuthenticationMeans.KEY_ID);
        expectedTypedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        //C4PO-9807 new set of auth means for OB
        expectedTypedAuthenticationMeans.put(CLIENT_ID_2, TypedAuthenticationMeans.CLIENT_ID_STRING);
        expectedTypedAuthenticationMeans.put(CLIENT_SECRET_2, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        expectedTypedAuthenticationMeans.put(TRANSPORT_PRIVATE_KID_2, TypedAuthenticationMeans.KEY_ID);
        expectedTypedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_2, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);

        // when
        Map<String, TypedAuthenticationMeans> actualTypedAuthenticationMeans = provider.getTypedAuthenticationMeans();

        // then
        assertThat(actualTypedAuthenticationMeans).isEqualTo(expectedTypedAuthenticationMeans);
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldThrowMissingAuthenticationMeansExceptionWhenMissingClientId(UrlDataProvider provider) {
        // given
        final UrlGetLoginRequest urlGetLoginRequest = createUrlGetLoginRequest(Collections.emptyMap());

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(MissingAuthenticationMeansException.class);
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider provider) {
        // given
        final UrlGetLoginRequest urlGetLoginRequest = createUrlGetLoginRequest(AUTH_MEANS);

        // when
        RedirectStep loginInfo = (RedirectStep) provider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(loginInfo).isNotNull();
        assertThat(loginInfo.getRedirectUrl()).endsWith("/oauth?client_id=THE-CLIENT-ID&redirect_uri=THE-REDIRECT-URL&scope_list=MEMBER_ACCT_INFO,FINS_STP_DTLS,FINS_BAL_INFO,FINS_TXN_INFO&state=loginState");
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldReturnFormWhenMissingAuthToken(UrlDataProvider provider) {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> provider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    // TODO Remove support for old parameter in ticket C4PO-9737
    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldCreateNewAccessMeansForRequestWithLowercaseAuthtoken(UrlDataProvider provider) throws IOException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?authtoken=" + CODE)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = provider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        TokenResponses oAuth2AccessTokens = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), TokenResponses.class);

        TokenResponse oAuth2AccessToken = oAuth2AccessTokens.getTokens().iterator().next();
        assertThat(oAuth2AccessToken.getAccessToken()).isEqualTo(TOKEN);
        assertThat(oAuth2AccessToken.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2AccessToken.getTokenType()).isEqualTo("mac");
        assertThat(oAuth2AccessToken.getMacKey()).isEqualTo("the-mac-key");
        assertThat(oAuth2AccessToken.getMacAlgorithm()).isEqualTo("hmac-sha-256");
        assertThat(oAuth2AccessToken.getScope()).isEqualTo("MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO");
    }

    // TODO Remove support for authToken parameter in ticket C4PO-9737
    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldCreateNewAccessMeansForRequestWithCamelCaseAuthToken(UrlDataProvider provider) throws IOException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?authToken=" + CODE)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = provider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        TokenResponses oAuth2AccessTokens = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), TokenResponses.class);

        TokenResponse oAuth2AccessToken = oAuth2AccessTokens.getTokens().iterator().next();
        assertThat(oAuth2AccessToken.getAccessToken()).isEqualTo(TOKEN);
        assertThat(oAuth2AccessToken.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2AccessToken.getTokenType()).isEqualTo("mac");
        assertThat(oAuth2AccessToken.getMacKey()).isEqualTo("the-mac-key");
        assertThat(oAuth2AccessToken.getMacAlgorithm()).isEqualTo("hmac-sha-256");
        assertThat(oAuth2AccessToken.getScope()).isEqualTo("MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO");
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldCreateNewAccessMeansForRequestWithCode(UrlDataProvider provider) throws IOException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + CODE)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = provider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        TokenResponses oAuth2AccessTokens = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), TokenResponses.class);

        TokenResponse oAuth2AccessToken = oAuth2AccessTokens.getTokens().iterator().next();
        assertThat(oAuth2AccessToken.getAccessToken()).isEqualTo(TOKEN);
        assertThat(oAuth2AccessToken.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2AccessToken.getTokenType()).isEqualTo("mac");
        assertThat(oAuth2AccessToken.getMacKey()).isEqualTo("the-mac-key");
        assertThat(oAuth2AccessToken.getMacAlgorithm()).isEqualTo("hmac-sha-256");
        assertThat(oAuth2AccessToken.getScope()).isEqualTo("MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO");
    }

    // TODO Remove parameters priority verification in ticket C4PO-9737
    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldCreateNewAccessMeansForRequestWithCodeAndAuthToken(UrlDataProvider provider) throws IOException {
        // given
        Map<String, BasicAuthenticationMean> authenticationMean = prepareMeansForParametersPriorityVerification();
        TestRestTemplateManager restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory,
                authenticationMean.get(CLIENT_TRANSPORT_KEY_ID_ROTATION).getValue());

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?authToken=" + CODE + "&code=" + CODE_NEW)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(authenticationMean)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = provider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        TokenResponses oAuth2AccessTokens = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), TokenResponses.class);

        TokenResponse oAuth2AccessToken = oAuth2AccessTokens.getTokens().iterator().next();
        assertThat(oAuth2AccessToken.getAccessToken()).isEqualTo(TOKEN);
        assertThat(oAuth2AccessToken.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2AccessToken.getTokenType()).isEqualTo("mac");
        assertThat(oAuth2AccessToken.getMacKey()).isEqualTo("the-mac-key");
        assertThat(oAuth2AccessToken.getMacAlgorithm()).isEqualTo("hmac-sha-256");
        assertThat(oAuth2AccessToken.getScope()).isEqualTo("MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO");
    }

    // TODO Remove support for authToken parameter in ticket C4PO-9737
    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenUnauthorizedNewAccessMeansForAuthTokenParameter(UrlDataProvider provider) {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?authtoken=" + WRONG_CODE)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenUnauthorizedNewAccessMeans(UrlDataProvider provider) {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + WRONG_CODE)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldThrowTokenInvalidExceptionWhenUsingInvalidRefreshToken(UrlDataProvider provider) throws IOException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID,
                "{\"tokens\":[{\"expires_in\":7193,\"access_token\":\"old-access-token\",\"refresh_token\":\"INVALID-REFRESH-TOKEN\",\"token_type\":\"mac\",\"scope\":\"MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO\",\"scope\":\"33f48435-06ae-42e1-816a-b80653562a56\",\"scope\":\"hmac-sha-256\"}]}",
                new Date(),
                new Date());

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(AUTH_MEANS)
                .setAccessMeans(accessMeansDTO)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldRefreshAccessMeansWhenDoProperRequest(UrlDataProvider provider) throws IOException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID,
                "{\"tokens\":[{\"expires_in\":7193,\"access_token\":\"old-access-token\",\"refresh_token\":\"THE-REFRESH-TOKEN\",\"token_type\":\"mac\",\"scope\":\"MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO\",\"scope\":\"33f48435-06ae-42e1-816a-b80653562a56\",\"scope\":\"hmac-sha-256\"}]}",
                new Date(),
                new Date());

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(AUTH_MEANS)
                .setAccessMeans(accessMeansDTO)
                .build();

        // when
        AccessMeansDTO accessMeans = provider.refreshAccessMeans(request);

        // then
        Assertions.assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        TokenResponses oAuth2AccessTokens = objectMapper.readValue(accessMeans.getAccessMeans(), TokenResponses.class);

        TokenResponse oAuth2AccessToken = oAuth2AccessTokens.getTokens().stream().findFirst().get();
        assertThat(oAuth2AccessToken.getAccessToken()).isEqualTo(TOKEN);
        assertThat(oAuth2AccessToken.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(oAuth2AccessToken.getTokenType()).isEqualTo("mac");
        assertThat(oAuth2AccessToken.getMacAlgorithm()).isEqualTo("hmac-sha-256");
        assertThat(oAuth2AccessToken.getMacKey()).isEqualTo("33f48435-06ae-42e1-816a-b80653562a56");
        assertThat(oAuth2AccessToken.getScope()).isEqualTo("MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO");
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldDoNotThrowExceptionWhenUserTokenDoProperRequest(UrlDataProvider provider) {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID,
                "{\"tokens\":[{\"expires_in\":7193,\"access_token\":\"access-token-to-revoke\",\"refresh_token\":\"THE-REFRESH-TOKEN\",\"token_type\":\"mac\",\"scope\":\"MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO\",\"scope\":\"33f48435-06ae-42e1-816a-b80653562a56\",\"scope\":\"hmac-sha-256\"}]}",
                new Date(),
                new Date());

        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(AUTH_MEANS)
                .setAccessMeans(accessMeansDTO)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(request);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldFetchProperNumberOfTransactions(UrlDataProvider provider) throws Exception {
        // given
        TokenResponses accessTokenResponses = createToken();

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(accessTokenResponses), new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.parse("2016-11-02T18:35:24.00Z"))
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        ProviderAccountDTO expectedAccount = accounts.get(0);
        expectedAccount.validate();
        List<ProviderTransactionDTO> transactions = expectedAccount.getTransactions();

        // 3 periods calls were made, 8 transactions each + 1 from the last + 1 call with 5 pending transactions
        // but filtered by 'transactionsFetchStartTime'
        assertThat(transactions).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldSkipAccountWithoutBalances(UrlDataProvider provider) throws Exception {
        // given
        TokenResponses accessTokenResponses = createToken();
        Map<String, BasicAuthenticationMean> authMeanWithDifferentClientId = new HashMap<>();
        authMeanWithDifferentClientId.putAll(AUTH_MEANS);
        authMeanWithDifferentClientId.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-CLIENT-ID-2"));

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(accessTokenResponses), new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.parse("2016-11-02T18:35:24.00Z"))
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(authMeanWithDifferentClientId)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(request);
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();

        //then
        assertThat(accounts).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldFetchDataWhenReturnProperResponse(UrlDataProvider provider) throws Exception {
        // given
        TokenResponses accessTokenResponses = createToken();

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(accessTokenResponses), new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                // this start time will be narrowed by implementation to 190 days in the past due to properties config
                .setTransactionsFetchStartTime(Instant.parse("2016-01-01T18:35:24.00Z"))
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        ProviderAccountDTO expectedAccount = accounts.get(0);
        expectedAccount.validate();
        List<ProviderTransactionDTO> transactions = expectedAccount.getTransactions();
        ProviderTransactionDTO firstTransactionToCheck = transactions.get(0);
        ExtendedTransactionDTO extendedTransaction = firstTransactionToCheck.getExtendedTransaction();

        assertThat(accounts).isNotEmpty();
        assertThat(expectedAccount.getAvailableBalance()).isNull();
        assertThat(expectedAccount.getCurrentBalance()).isEqualTo("-55000.00");
        assertThat(expectedAccount).isEqualToIgnoringGivenFields(expectedAccount, "transactions", "lastRefreshed");
        // 3 periods calls were made, 8 transactions each + 1 from the last + 1 call with 5 pending transactions:
        assertThat(transactions).hasSize(30);

        assertThat(firstTransactionToCheck).isEqualToIgnoringGivenFields(expectedAccount.getTransactions().get(0),
                "transactionId", "extendedTransaction");
        assertThat(firstTransactionToCheck.getAmount()).isEqualByComparingTo("210.2");
        assertThat(firstTransactionToCheck.getStatus()).isEqualTo(PENDING);
        assertThat(firstTransactionToCheck.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(firstTransactionToCheck.getDateTime()).isEqualTo(AmexDateTimeUtils.getZonedDateTime("2016-11-10"));

        ProviderTransactionDTO secondTransactionToCheck = transactions.get(10);
        assertThat(secondTransactionToCheck.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(secondTransactionToCheck.getStatus()).isEqualTo(BOOKED);
        assertThat(secondTransactionToCheck.getAmount()).isEqualByComparingTo("110.25");
        assertThat(secondTransactionToCheck.getDateTime()).isEqualTo(AmexDateTimeUtils.getZonedDateTime("2016-08-10"));

        assertExtendedAccounts(accounts);
        assertExtendedTransaction(extendedTransaction);
        assertOriginalAmountInExtendedModel(transactions);
    }

    private void assertExtendedAccounts(final List<ProviderAccountDTO> accounts) {
        ExtendedAccountDTO account = accounts.get(0).getExtendedAccount();
        assertThat(account.getResourceId()).isEqualTo("XXXX-XXXXXX-81004");
        assertThat(account.getAccountReferences()).extracting(AccountReferenceDTO::getType, AccountReferenceDTO::getValue).contains(tuple(MASKED_PAN, "XXXX-XXXXXX-81004"));
        assertThat(account.getCurrency()).isEqualTo(AUD);
        assertThat(account.getName()).isEqualTo("Credit Card");
        assertThat(account.getProduct()).isEqualTo("Product description");
        assertThat(account.getStatus()).isEqualTo(ENABLED);
        assertThat(account.getBalances()).extracting(it -> it.getBalanceAmount().getAmount(), it -> it.getBalanceAmount().getCurrency(), BalanceDTO::getBalanceType).contains(tuple(new BigDecimal("55000.00"), AUD, AVAILABLE));
    }

    private void assertExtendedTransaction(final ExtendedTransactionDTO transaction) {
        assertThat(transaction.getStatus()).isEqualTo(PENDING);
        assertThat(transaction.getEntryReference()).isEqualTo("320162240910370230");
        assertThat(transaction.getBookingDate()).isEqualTo(AmexDateTimeUtils.getZonedDateTime("2016-08-10"));
        assertThat(transaction.getValueDate()).isEqualTo(AmexDateTimeUtils.getZonedDateTime("2016-11-10"));
        assertThat(transaction.getTransactionAmount()).extracting(BalanceAmountDTO::getCurrency, BalanceAmountDTO::getAmount).contains(USD, new BigDecimal("-210.20"));
        assertThat(transaction.getCreditorName()).isEqualTo("NigelSmythe");
        assertThat(transaction.getDebtorName()).isEqualTo("NIGEL'S BAGEL EMPORIUM 18631 N 19TH AVE, 150");
        assertThat(transaction.getRemittanceInformationUnstructured()).isEqualTo("NIGEL'S BAGEL EMPORIUM 194 0194");
        assertThat(transaction.getExchangeRate().get(0)).extracting(ExchangeRateDTO::getCurrencyFrom, ExchangeRateDTO::getRateFrom).contains(PLN, "3.6");
        assertThat(transaction.isTransactionIdGenerated()).isFalse();
    }

    private void assertOriginalAmountInExtendedModel(final List<ProviderTransactionDTO> transactions) {
        BalanceAmountDTO mappedOriginalAmount = transactions.get(5).getExtendedTransaction().getOriginalAmount();
        assertThat(mappedOriginalAmount.getAmount()).isEqualTo(BigDecimal.valueOf(4.56));
        assertThat(mappedOriginalAmount.getCurrency()).isEqualTo(USD);

        BalanceAmountDTO noOriginalAmount = transactions.get(6).getExtendedTransaction().getOriginalAmount();
        assertThat(noOriginalAmount).isNull();

        BalanceAmountDTO noOriginalAmountWhenAmountIsMissing = transactions.get(7).getExtendedTransaction().getOriginalAmount();
        assertThat(noOriginalAmountWhenAmountIsMissing).isNull();

        BalanceAmountDTO noOriginalAmountWhenCurrencyIsMissing = transactions.get(8).getExtendedTransaction().getOriginalAmount();
        assertThat(noOriginalAmountWhenCurrencyIsMissing).isNull();

        BalanceAmountDTO mappedOriginalAmountWithComma = transactions.get(9).getExtendedTransaction().getOriginalAmount();
        assertThat(mappedOriginalAmountWithComma.getAmount()).isEqualTo(BigDecimal.valueOf(1.23));
        assertThat(mappedOriginalAmountWithComma.getCurrency()).isEqualTo(USD);

        BalanceAmountDTO mappedOriginalAmountInUkFormat = transactions.get(10).getExtendedTransaction().getOriginalAmount();
        assertThat(mappedOriginalAmountInUkFormat.getAmount()).isEqualTo(new BigDecimal("391000.00"));
        assertThat(mappedOriginalAmountInUkFormat.getCurrency()).isEqualTo(USD);

        BalanceAmountDTO mappedOriginalAmountInItalianFormat = transactions.get(11).getExtendedTransaction().getOriginalAmount();
        assertThat(mappedOriginalAmountInItalianFormat.getAmount()).isEqualTo(new BigDecimal("123000.00"));
        assertThat(mappedOriginalAmountInItalianFormat.getCurrency()).isEqualTo(USD);

        BalanceAmountDTO mappedOriginalAmountInIncorrectFormat = transactions.get(12).getExtendedTransaction().getOriginalAmount();
        assertThat(mappedOriginalAmountInIncorrectFormat).isNull();
    }

    @ParameterizedTest
    @MethodSource("getAmexGroupDataProviders")
    public void shouldThrowTokenInvalidExceptionWhenResponseStatusIs401(UrlDataProvider provider) throws Exception {
        // given
        TokenResponses accessTokenResponses = createToken();
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(accessTokenResponses), new Date(), new Date());

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.parse("2016-12-12T18:35:24.00Z"))
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        stubMappings.add(stubFor(get(urlEqualTo("/servicing/v1/member/accounts")).willReturn(aResponse().withStatus(401).withBody("unauthorized"))));

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> provider.fetchData(request);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }

    private UrlGetLoginRequest createUrlGetLoginRequest(final Map<String, BasicAuthenticationMean> authenticationMeans) {
        return new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("THE-REDIRECT-URL").setState("loginState")
                .setAuthenticationMeansReference(mock(AuthenticationMeansReference.class))
                .setAuthenticationMeans(authenticationMeans)
                .setExternalConsentId("externalConsentId")
                .setSigner(mock(Signer.class))
                .setRestTemplateManager(mock(RestTemplateManager.class))
                .setSigner(signer)
                .setPsuIpAddress("127.0.0.1")
                .build();
    }

    public static Map<String, BasicAuthenticationMean> prepareBasicMeans() {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(CLIENT_SECRET, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), "THE-CLIENT-SECRET"));
        means.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificates()));
        return means;
    }

    public static Map<String, BasicAuthenticationMean> prepareMeans() {
        Map<String, BasicAuthenticationMean> means = prepareBasicMeans();
        means.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-CLIENT-ID"));
        return means;
    }

    public static Map<String, BasicAuthenticationMean> prepareMeansForParametersPriorityVerification() {
        Map<String, BasicAuthenticationMean> means = prepareBasicMeans();
        means.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE-NEW-CLIENT-ID"));
        return means;
    }

    private static String readCertificates() {
        try {
            URI fileURI = AmexGroupDataProviderIntegrationTest.class
                    .getClassLoader()
                    .getResource("certificates/yolt_certificate.pem")
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private TokenResponses createToken() {
        TokenResponse accessTokenResponse = new TokenResponse();
        accessTokenResponse.setAccessToken("9shWQjwZ0jRPIN1T2NrnQYw");
        accessTokenResponse.setMacAlgorithm("hmac-sha-256");
        accessTokenResponse.setTokenType("mac");
        accessTokenResponse.setScope("MEMBER_ACCT_INFO FINS_STP_DTLS FINS_BAL_INFO FINS_TXN_INFO");
        accessTokenResponse.setMacKey("33f48435-06ae-42e1-816a-b80653562a56");
        TokenResponses accessTokenResponses = new TokenResponses();
        accessTokenResponses.getTokens().add(accessTokenResponse);
        return accessTokenResponses;
    }
}
