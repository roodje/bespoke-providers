package com.yolt.providers.openbanking.ais.tescobank.ais.v3.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankApp;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleAccessMeansV2;
import com.yolt.providers.openbanking.ais.tescobank.TescoSampleTypedAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.utils.JwtHelper;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.utils.UriHelper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in provider.
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
@SpringBootTest(classes = {TescoBankApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tescobank/ais-3.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("tescobank")
public class TescoDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final Signer SIGNER = new SignerMock();
    private static final String REQUEST_TRACE_ID = UUID.randomUUID().toString();
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    private static String SERIALIZED_ACCESS_MEANS;

    private RestTemplateManagerMock restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("TescoBankDataProviderV7")
    private GenericBaseDataProviderV2 tescoBankDataProviderV7;

    private Stream<GenericBaseDataProviderV2> getProviders() {
        return Stream.of(tescoBankDataProviderV7);
    }

    @BeforeAll
    public static void beforeAll() throws JsonProcessingException {
        SERIALIZED_ACCESS_MEANS = TescoSampleAccessMeansV2.getSerializedAccessMeans();
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = TescoSampleTypedAuthenticationMeansV2.getTypedAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> REQUEST_TRACE_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnRedirectStepWithConsentUrl(GenericBaseDataProviderV2 provider) throws MalformedClaimException, UnsupportedEncodingException {
        // given
        final String loginState = UUID.randomUUID().toString();
        final String redirectUrlEncoded = URLEncoder.encode(TEST_REDIRECT_URL, StandardCharsets.UTF_8.name());

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(TEST_REDIRECT_URL)
                .setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(3600);

        // when
        RedirectStep loginInfo = (RedirectStep) provider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
        String redirectUrl = loginInfo.getRedirectUrl();
        assertThat(redirectUrl).isNotEmpty();

        Map<String, String> queryParams = UriHelper.extractQueryParams(redirectUrl);
        JwtClaims jwtClaims = JwtHelper.parseJwtClaims(queryParams.get("request"));

        assertThat(jwtClaims.getIssuer()).isEqualTo("someClientId");
        assertThat(jwtClaims.getAudience()).containsOnly("audience");
        assertThat(queryParams)
                .containsOnlyKeys("nonce", "response_type", "client_id", "scope", "state", "redirect_uri", "request")
                .hasEntrySatisfying("nonce", nonce ->
                        assertThat(nonce)
                                .isEqualTo(loginState)
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "nonce")))
                .hasEntrySatisfying("response_type", responseType ->
                        assertThat(responseType)
                                .isEqualTo("code+id_token")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "response_type").replace(" ", "+")))
                .hasEntrySatisfying("client_id", clientId ->
                        assertThat(clientId)
                                .isEqualTo("someClientId")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "client_id")))
                .hasEntrySatisfying("scope", scope ->
                        assertThat(scope)
                                .isEqualTo("openid+accounts")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "scope").replace(" ", "+")))
                .hasEntrySatisfying("state", state ->
                        assertThat(state)
                                .isEqualTo(loginState)
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "state")))
                .hasEntrySatisfying("redirect_uri", redirectUri ->
                        assertThat(redirectUri)
                                .isEqualTo(redirectUrlEncoded)
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "redirect_uri").replace(":", "%3A").replace("/", "%2F")));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldSuccessfullyFetchData(GenericBaseDataProviderV2 provider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts().size()).isEqualTo(3);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        //Verify Current Account
        ProviderAccountDTO currentAccount = dataProviderResponse.getAccounts().get(0);
        validateCurrentAccount(currentAccount);

        //Verify Savings Account
        ProviderAccountDTO savingsAccount = dataProviderResponse.getAccounts().get(1);
        validateSavingsAccount(savingsAccount);

        //Verify Credit Card
        ProviderAccountDTO creditAccount = dataProviderResponse.getAccounts().get(2);
        validateCreditCardAccount(creditAccount);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnRefreshedAccessMeans(GenericBaseDataProviderV2 provider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(), "my_access_means",
                new Date(),
                new Date());
        accessMeans.setAccessMeans(SERIALIZED_ACCESS_MEANS);
        accessMeans.setUserId(USER_ID);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO resultAccessMeans = provider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);

        AccessMeansState refreshedAccessMeansState = OpenBankingTestObjectMapper.INSTANCE.readValue(resultAccessMeans.getAccessMeans(), AccessMeansState.class);
        assertThat(refreshedAccessMeansState.getAccessMeans().getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(refreshedAccessMeansState.getAccessMeans().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateNewAccessMeans(GenericBaseDataProviderV2 provider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "BncpkVcaQsT8aBEJe6cea5n5by3mcNkE9rhx-KJn";
        String redirectUrl = "https://www.yolt.com/callback/ac75d67d-5ede-4972-94a8-3b8481fa2145?code=" + authorizationCode + "&state=secretState";
        Date _29MinutesFromNow = Date.from(Instant.now().plus(4, ChronoUnit.MINUTES));

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setProviderState("""
                        {"permissions":["ReadAccountsDetail",\
                        "ReadBalances",\
                        "ReadDirectDebits",\
                        "ReadProducts",\
                        "ReadStandingOrdersDetail",\
                        "ReadTransactionsCredits",\
                        "ReadTransactionsDebits",\
                        "ReadTransactionsDetail"]}\
                        """)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = provider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_29MinutesFromNow.before(accessMeansDTO.getExpireTime())).isTrue();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);

        AccessMeansState accessMeansState = OpenBankingTestObjectMapper.INSTANCE.readValue(accessMeansDTO.getAccessMeans(), AccessMeansState.class);
        assertThat(accessMeansState.getAccessMeans().getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(accessMeansState.getAccessMeans().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldDoNotThrowAnyExceptionOnUserSiteDelete(GenericBaseDataProviderV2 provider) {
        // given
        String externalConsentId = "363ca7c1-9d03-4876-8766-ddefc9fd2d76";
        UrlOnUserSiteDeleteRequest urlGetLogin = createUrlOnUserSiteDeleteRequest(externalConsentId);

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private UrlOnUserSiteDeleteRequest createUrlOnUserSiteDeleteRequest(final String externalConsentId) {
        return new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();
    }

    private void validateCurrentAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("10000000000001449160");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("-6.45"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("8.30"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("10272100577464");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getName()).isEqualTo("Tesco Bank Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders().size()).isEqualTo(2);
        StandingOrderDTO standingOrderDTO = providerAccountDTO.getStandingOrders().get(0);
        assertThat(standingOrderDTO.getDescription()).isEmpty();
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("530.00");
        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("20269601871019");

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits().size()).isEqualTo(1);
        DirectDebitDTO directDebitDTO = providerAccountDTO.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDescription()).isEqualTo("GREENWICH LEISURE");
        assertThat(directDebitDTO.isDirectDebitStatus()).isFalse();
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("20.95");

        validateCurrentTransactions(providerAccountDTO.getTransactions());
    }

    private void validateCurrentTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions.size()).isEqualTo(5);

        ProviderTransactionDTO pendingTransaction = transactions.get(0);
        assertThat(pendingTransaction.getAmount()).isEqualTo(new BigDecimal("0.09"));
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(pendingTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(pendingTransaction.getDateTime()).isEqualTo("2020-12-01T00:00Z[Europe/London]");
        ExtendedTransactionDTO extendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-12-01T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-12-01T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(new BigDecimal("0.09"));
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("INTEREST PAID");
        pendingTransaction.validate();

        ProviderTransactionDTO bookedTransaction = transactions.get(1);
        assertThat(bookedTransaction.getAmount()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(bookedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(bookedTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(bookedTransaction.getDateTime()).isEqualTo("2020-11-28T08:31Z[Europe/London]");
        extendedTransaction = bookedTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-28T08:31Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-30T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(new BigDecimal("-2000.00"));
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("MOBILE-CHANNEL");
        bookedTransaction.validate();
    }

    private void validateSavingsAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("10000000000000947108");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("10.00"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("10.00"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB15AIBK12345678901235");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getName()).isEqualTo("Tesco Bank Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        assertThat(providerAccountDTO.getTransactions()).isEmpty();
    }

    private void validateCreditCardAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("0002");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("-6.45"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("8.30"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber()).isNull();
        assertThat(providerAccountDTO.getName()).isEqualTo("MR ROBIN HOOD");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isEqualTo("************0002");
        assertThat(providerAccountDTO.getCreditCardData().getAvailableCreditAmount()).isEqualTo(new BigDecimal("-6.45"));

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        assertThat(providerAccountDTO.getTransactions().size()).isEqualTo(3);
    }
}