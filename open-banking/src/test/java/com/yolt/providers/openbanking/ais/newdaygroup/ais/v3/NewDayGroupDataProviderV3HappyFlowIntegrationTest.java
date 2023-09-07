package com.yolt.providers.openbanking.ais.newdaygroup.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupApp;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.newdaygroup.NewDayGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard.AmazonCreditCardDataProviderV3;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.http.client.utils.URIBuilder;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains all happy flows occurring in New Day Group providers.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {NewDayGroupApp.class,
        OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("newdaygroup-v1")
@AutoConfigureWireMock(stubs = "classpath:/stubs/newdaygroup/ais-3.1/happy-flow/", httpsPort = 0, port = 0)
public class NewDayGroupDataProviderV3HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "https://www.yolt.com/test";
    private static AccessMeans ACCESS_MEANS;

    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    @Qualifier("AmazonCreditCardDataProviderV3")
    private AmazonCreditCardDataProviderV3 amazonDataProvider;

    @Autowired
    @Qualifier("AquaCreditCardDataProviderV3")
    private GenericBaseDataProvider aquaDataProvider;

    @Autowired
    @Qualifier("ArgosDataProviderV3")
    private GenericBaseDataProvider argosDataProvider;

    @Autowired
    @Qualifier("HouseOfFaserDataProviderV3")
    private GenericBaseDataProvider fraserDataProvider;

    @Autowired
    @Qualifier("MarblesDataProviderV3")
    private GenericBaseDataProvider marblesDataProvider;

    @Autowired
    @Qualifier("DebenhamsDataProviderV3")
    private GenericBaseDataProvider debenhamsDataProvider;

    public Stream<UrlDataProvider> getNewDayDataProviders() {
        return Stream.of(amazonDataProvider,
                aquaDataProvider,
                argosDataProvider,
                fraserDataProvider,
                marblesDataProvider,
                debenhamsDataProvider);
    }

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() {
        Instant now = Instant.now();
        ACCESS_MEANS = new AccessMeans(
                now,
                USER_ID,
                "fake-access-token",
                "fake-refresh-token",
                Date.from(now.plus(1, ChronoUnit.DAYS)),
                Date.from(now),
                REDIRECT_URL);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new NewDayGroupSampleAuthenticationMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), any()))
                .thenReturn(new NewDayGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider dataProvider) throws URISyntaxException {
        // given
        String clientId = "fake-client-id";
        String loginState = UUID.randomUUID().toString();
        String redirectUrl = "http://yolt.com/identifier";

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(redirectUrl).setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .build();

        List<String> expectedQueryParams = List.of("consentid=7ce38ef1-1433-456a-b06c-93f7fe2badc4",
                "response_type=code id_token",
                "client_id=" + clientId,
                "state=" + loginState,
                "scope=openid accounts offline_access",
                "nonce=" + loginState,
                "redirect_uri=" + redirectUrl,
                "request=ZW5jb2RlZHNpZ25hdHVyZQ==");

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        List<String> actualQueryParameters = new URIBuilder(loginInfo.getRedirectUrl())
                .getQueryParams()
                .stream()
                .map(queryParam -> queryParam.getName() + "=" + queryParam.getValue())
                .collect(Collectors.toList());

        assertThat(actualQueryParameters).containsExactlyInAnyOrder(expectedQueryParams.toArray(new String[expectedQueryParams.size()]));
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("7ce38ef1-1433-456a-b06c-93f7fe2badc4");
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldCorrectlyRefreshAccessMeans(UrlDataProvider dataProvider) throws Exception {
        // given
        Date now = new Date();
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), now, now);
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        AccessMeans deserializedOAuthToken = objectMapper.readValue(retrievedAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("refreshed-fake-token");

        // C4PO 4604 - newday send expires_in = 600 but the real value is 300
        assertThat(deserializedOAuthToken.getExpireTime().toInstant())
                .isAfter(now.toInstant().plusSeconds(200))
                .isBefore(now.toInstant().plusSeconds(400));
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) {
        // given
        Date now = new Date();
        UUID userId = UUID.randomUUID();
        String authorizationCode = "fake-auth-code";
        String redirectUrl = "https://www.yolt.com/callback/81c0374e-838a-42dc-8739-6f04b268a17c#code=" + authorizationCode + "&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(newAccessMeans).isNotNull();
        // C4PO 4604 - newday send expires_in = 600 but the real value is 300
        assertThat(newAccessMeans.getAccessMeans().getExpireTime().toInstant())
                .isAfter(now.toInstant().plusSeconds(200))
                .isBefore(now.toInstant().plusSeconds(400));
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionWhenErrorIsInvalidGrant(UrlDataProvider dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setRedirectUrlPostedBackFromSite("http://example.com?error=invalid_grant")
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AssertionsForClassTypes.assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldDeleteUserSite(UrlDataProvider dataProvider) {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("7ce38ef1-1433-456a-b06c-93f7fe2badc4")
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowableAssert.ThrowingCallable runnable = () -> dataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(runnable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getNewDayDataProviders")
    public void shouldFetchData(UrlDataProvider dataProvider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getName()).isEqualTo("Odin");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("-100.00");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);

        // Verify Transactions
        assertThat(providerAccountDTO.getTransactions()).hasSize(11);

        ProviderTransactionDTO firstTransaction = providerAccountDTO.getTransactions().get(0);
        assertThat(firstTransaction.getAmount()).isEqualTo("100.00");
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(firstTransaction.getDescription()).isEqualTo("Cash from Aubrey");
        assertThat(firstTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstTransaction.getDateTime()).isEqualTo("2018-01-01T00:00:00+00:00");
        ExtendedTransactionDTO extendedTransaction = firstTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2018-01-01T00:00:00+00:00");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("100.00");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Cash from Aubrey");

        ProviderTransactionDTO secondTransaction = providerAccountDTO.getTransactions().get(1);
        assertThat(secondTransaction.getAmount()).isEqualTo("99.99");
        assertThat(secondTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(secondTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);

        providerAccountDTO = dataProviderResponse.getAccounts().get(1);
        assertThat(providerAccountDTO.getName()).isEqualTo(dataProvider.getProviderIdentifierDisplayName() + " Account");
    }

    private String getSerializedAccessMeans() {
        try {
            return objectMapper.writeValueAsString(ACCESS_MEANS);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken", e);
        }
    }
}