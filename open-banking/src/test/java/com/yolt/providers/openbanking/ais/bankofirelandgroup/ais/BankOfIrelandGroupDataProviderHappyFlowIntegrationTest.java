package com.yolt.providers.openbanking.ais.bankofirelandgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandGroupApp;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandRoiSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.BankOfIrelandSampleTypedAuthMeans;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
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

import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper.*;
import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi.auth.BankOfIrelandRoiAuthMeansMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in Bank of Ireland provider.
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
@SpringBootTest(classes = {BankOfIrelandGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankofireland/ais-3.0.0/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("bankofireland")
public class BankOfIrelandGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "http://yolt.com/identifier";
    private static String SERIALIZED_ACCESS_MEANS;
    private static final Signer SIGNER = new SignerMock();

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    @Qualifier("BankOfIrelandDataProviderV7")
    private BankOfIrelandGroupBaseDataProvider bankOfIrelandDataProviderV7;

    @Autowired
    @Qualifier("BankOfIrelandRoiDataProvider")
    private BankOfIrelandGroupBaseDataProvider bankOfIrelandRoiDataProvider;

    private Stream<Arguments> getProvidersWithSampleAuthMeans() {
        return Stream.of(
                Arguments.of(bankOfIrelandDataProviderV7, BankOfIrelandSampleTypedAuthMeans.getSampleAuthMeans()),
                Arguments.of(bankOfIrelandRoiDataProvider, BankOfIrelandRoiSampleTypedAuthMeans.getSampleAuthMeans())
        );
    }

    private Stream<BankOfIrelandGroupBaseDataProvider> getUkProviders() {
        return Stream.of(bankOfIrelandDataProviderV7);
    }

    private Stream<BankOfIrelandGroupBaseDataProvider> getIeProviders() {
        return Stream.of(bankOfIrelandRoiDataProvider);
    }

    @BeforeAll
    public static void beforeAll() throws JsonProcessingException {
        AccessMeans accessMeans = new AccessMeans(
                Instant.now(),
                USER_ID,
                "j4HmjDMaBdSXUQQzN1GpdHoozGho",
                "xcdog81ShrtxuIqRO9Zlf0zG0YVuaGuo9iK8sHWeA8",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL
        );
        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(accessMeans);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "5008e82d-f5ac-42fe-8f07-a49b025f3c2e";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @ParameterizedTest
    @MethodSource("getUkProviders")
    public void shouldReturnTransportKeyRequirementsForUkProviders(BankOfIrelandGroupBaseDataProvider subject) {
        // when
        KeyRequirements transportKeyRequirements = subject.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TRANSPORT_CERTIFICATE_NAME_V2).get());
    }

    @ParameterizedTest
    @MethodSource("getIeProviders")
    public void shouldReturnTransportKeyRequirementsForIeProviders(BankOfIrelandGroupBaseDataProvider subject) {
        // when
        KeyRequirements transportKeyRequirements = subject.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_CHAIN_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getUkProviders")
    public void shouldReturnSigningKeyRequirementsForUkProviders(BankOfIrelandGroupBaseDataProvider subject) {
        // when
        KeyRequirements signingKeyRequirements = subject.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME_V2).get());
    }

    @ParameterizedTest
    @MethodSource("getIeProviders")
    public void shouldReturnSigningKeyRequirementsForIeProviders(BankOfIrelandGroupBaseDataProvider subject) {
        // when
        KeyRequirements signingKeyRequirements = subject.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getUkProviders")
    public void shouldReturnTypedAuthenticationMeansForUkProviders(BankOfIrelandGroupBaseDataProvider subject) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = subject.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME_V2,
                CLIENT_ID_NAME_V2,
                SIGNING_KEY_HEADER_ID_NAME_V2,
                SIGNING_PRIVATE_KEY_ID_NAME_V2,
                TRANSPORT_CERTIFICATE_NAME_V2,
                TRANSPORT_PRIVATE_KEY_ID_NAME_V2,
                SOFTWARE_ID_NAME_V2,
                SOFTWARE_STATEMENT_ASSERTION_NAME_V2
        );
    }

    @ParameterizedTest
    @MethodSource("getIeProviders")
    public void shouldReturnTypedAuthenticationMeansForIeProviders(BankOfIrelandGroupBaseDataProvider subject) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = subject.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_CHAIN_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                SOFTWARE_ID_NAME,
                SOFTWARE_STATEMENT_ASSERTION_NAME
        );
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldReturnRedirectStepWithConsentUrl(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) throws MalformedClaimException, UnsupportedEncodingException {
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
        RedirectStep loginInfo = (RedirectStep) subject.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
        String redirectUrl = loginInfo.getRedirectUrl();
        assertThat(redirectUrl).isNotEmpty();

        Map<String, String> queryParams = UriHelper.extractQueryParams(redirectUrl);
        JwtClaims jwtClaims = JwtHelper.parseJwtClaims(queryParams.get("request"));

        assertThat(jwtClaims.getIssuer()).isEqualTo("someClientId");
        assertThat(jwtClaims.getAudience()).containsOnly("audience");
        assertThat(jwtClaims.getExpirationTime()).isNotNull();
        assertThat(jwtClaims.getExpirationTime().getValue()).isGreaterThanOrEqualTo(expirationDate.getValue());
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
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldSuccessfullyFetchData(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) throws Exception {
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
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

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
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldReturnRefreshedAccessMeans(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) throws Exception {
        // given
        requestTraceId = "82802014-b8ee-497f-88fa-848187020e9d";

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
        AccessMeansDTO resultAccessMeans = subject.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);
        AccessMeans refreshedAccessMeans = OpenBankingTestObjectMapper.INSTANCE.readValue(resultAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(refreshedAccessMeans.getAccessToken()).isEqualTo("0002qSaik8FeZuXPd6E79EAb9Uh2");
        assertThat(refreshedAccessMeans.getRefreshToken()).isEqualTo("xcdog81ShrtxuIqRO9Zlf0zG0YVuaGuo9iK8sHWeA8");
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldCreateNewAccessMeans(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) throws Exception {
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
                .build();

        // when
        AccessMeansDTO accessMeansDTO = subject.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_29MinutesFromNow.before(accessMeansDTO.getExpireTime())).isTrue();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);

        AccessMeans accessMeans = OpenBankingTestObjectMapper.INSTANCE.readValue(accessMeansDTO.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("j4HmjDMaBdSXUQQzN1GpdHoozGho");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("xcdog81ShrtxuIqRO9Zlf0zG0YVuaGuo9iK8sHWeA8");
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSampleAuthMeans")
    public void shouldSuccessfullyRemoveUserConsentOnUserSiteDelete(BankOfIrelandGroupBaseDataProvider subject, Map<String, BasicAuthenticationMean> authenticationMeans) {
        // given
        String externalConsentId = "50ca5ed5-317c-451c-8438-3b3fb91466e1";
        UrlOnUserSiteDeleteRequest urlGetLogin = createUrlOnUserSiteDeleteRequest(externalConsentId, authenticationMeans);

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> subject.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private UrlOnUserSiteDeleteRequest createUrlOnUserSiteDeleteRequest(final String externalConsentId, Map<String, BasicAuthenticationMean> authenticationMeans) {
        return new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();
    }

    private void validateCurrentAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("5be63cf5-1e7d-45f9-b15d-db1dcc6d979e");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("-99.59"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("-99.59"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("90489636534098");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getName()).isEqualTo("Current Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders().size()).isEqualTo(1);
        assertThat(providerAccountDTO.getStandingOrders().get(0).getDescription()).isEqualTo("HL");
        assertThat(providerAccountDTO.getStandingOrders().get(0).getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(providerAccountDTO.getStandingOrders().get(0).getNextPaymentAmount()).isEqualTo(new BigDecimal("60.0"));
        assertThat(providerAccountDTO.getStandingOrders().get(0).getCounterParty().getIdentification()).isEqualTo("609XXXXXXXX908");

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        validateCurrentTransactions(providerAccountDTO.getTransactions());
    }

    private void validateCurrentTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions.size()).isEqualTo(4);

        ProviderTransactionDTO pendingTransaction = transactions.get(0);
        assertThat(pendingTransaction.getAmount()).isEqualTo(new BigDecimal("12.45"));
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(pendingTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(pendingTransaction.getDateTime()).isEqualTo("2020-11-12T00:00Z[Europe/London]");
        ExtendedTransactionDTO extendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-12T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-12T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(new BigDecimal("-12.45"));
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("SPAR");
        pendingTransaction.validate();

        ProviderTransactionDTO bookedTransaction = transactions.get(1);
        assertThat(bookedTransaction.getAmount()).isEqualTo(new BigDecimal("28.81"));
        assertThat(bookedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(bookedTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(bookedTransaction.getDateTime()).isEqualTo("2020-11-11T00:00Z[Europe/London]");
        extendedTransaction = bookedTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-11T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-11T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(new BigDecimal("28.81"));
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("STORES");
        bookedTransaction.validate();
    }

    private void validateSavingsAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("f59f6450-d6de-4f08-917a-d883cae85119");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("10.00"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("10.00"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("90039587636234");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getName()).isEqualTo("Travel");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        assertThat(providerAccountDTO.getTransactions()).isEmpty();
    }

    private void validateCreditCardAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("82802014-b8ee-497f-88fa-848187020e9d");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("12.50"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("-13.37"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber()).isNull();
        assertThat(providerAccountDTO.getName()).isEqualTo("Bank Of Ireland Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isEqualTo("XXXX XXXX XXXX 2343");
        assertThat(providerAccountDTO.getCreditCardData().getAvailableCreditAmount()).isEqualTo(new BigDecimal("12.50"));

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        assertThat(providerAccountDTO.getTransactions().size()).isEqualTo(2);
    }
}