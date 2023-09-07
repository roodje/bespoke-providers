package com.yolt.providers.openbanking.ais.nationwide.ais.v10;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideDataProviderV10;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultPermissions.DEFAULT_PERMISSIONS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Nationwide provider.
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 */
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/nationwide/ais-3.1.2/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("nationwide")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NationwideDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "http://yolt.com/identifier";
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static String SERIALIZED_ACCESS_MEANS_OUTSIDE_CONSENT_WINDOW;
    private static String SERIALIZED_ACCESS_MEANS;
    private static final ZoneId zoneId = ZoneId.of("Europe/London");

    private RestTemplateManagerMock restTemplateManagerMock;

    private final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;

    @Autowired
    @Qualifier("NationwideDataProviderV11")
    private NationwideDataProviderV10 nationwideDataProviderV11;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(nationwideDataProviderV11);
    }

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        AccessMeansState<AccessMeans> accessMeansState = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of("ReadParty"));
        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(accessMeansState);

        AccessMeansState<AccessMeans> accessTokenOutsideConsentWindow = new AccessMeansState<>(new AccessMeans(
                Instant.ofEpochMilli(0L),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of("ReadParty"));

        SERIALIZED_ACCESS_MEANS_OUTSIDE_CONSENT_WINDOW = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(accessTokenOutsideConsentWindow);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new NationwideSampleAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider provider) {
        // given
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        String expectedUrlRegex = "?response_type=code+id_token&client_id=" + clientId + "&state=" +
                loginState + "&scope=openid+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=";

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL).setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(new SignerMock())
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) provider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getRedirectUrl()).contains(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("ACC_1_2_38437330-353e-13eb-a38c-3fc3aeddf3bc");
        assertThat(loginInfo.getProviderState()).isEqualTo("""
                {"permissions":["ReadParty",\
                "ReadAccountsDetail",\
                "ReadBalances",\
                "ReadDirectDebits",\
                "ReadProducts",\
                "ReadStandingOrdersDetail",\
                "ReadTransactionsCredits",\
                "ReadTransactionsDebits",\
                "ReadTransactionsDetail"]}\
                """);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnCorrectlyFetchData(UrlDataProvider provider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(5);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        //Verify Current Account
        ProviderAccountDTO currentAccount = dataProviderResponse.getAccounts().get(0);
        validateCurrentAccountWithHolderName(currentAccount, "User Name");

        // Verify Standing Order
        assertThat(currentAccount.getStandingOrders()).hasSize(1);
        StandingOrderDTO standingOrderDTO = currentAccount.getStandingOrders().get(0);
        assertThat(standingOrderDTO.getDescription()).isEqualTo("HELP TO BUY");
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofDays(7));
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("530.00");
        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("113357-12038161");

        // Verify Direct Debit
        assertThat(currentAccount.getDirectDebits()).hasSize(1);
        DirectDebitDTO directDebitDTO = currentAccount.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDescription()).isEqualTo("O2");
        assertThat(directDebitDTO.isDirectDebitStatus()).isTrue();
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("21.53");

        validateCurrentTransactions(currentAccount.getTransactions());

        //Verify Credit Card
        ProviderAccountDTO creditAccount = dataProviderResponse.getAccounts().get(1);
        validateCreditCardAccount(creditAccount);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnCorrectlyFetchDataWhenNoStandingOrdersDirectDebits(UrlDataProvider provider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS_OUTSIDE_CONSENT_WINDOW, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(5);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        //Verify Current Account
        ProviderAccountDTO currentAccount = dataProviderResponse.getAccounts().get(0);
        validateCurrentAccountWithoutHolderName(currentAccount);

        // Verify Standing Orders
        //We want to ignore standing orders and direct debits since Nationwide require too often re-authentication for them
        assertThat(currentAccount.getStandingOrders()).isEmpty();

        // Verify Direct Debits
        //We want to ignore standing orders and direct debits since Nationwide require too often re-authentication for them
        assertThat(currentAccount.getDirectDebits()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnFetchDataWithSpecificChangesForCreditCards(UrlDataProvider provider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(5);

        //validate credit card account with no PAN and no nickname
        ProviderAccountDTO creditCardAccountWithNoPANAndNoNickname = dataProviderResponse.getAccounts().get(2);
        validateCreditCardAccountWithNoPANAndNoNickname(creditCardAccountWithNoPANAndNoNickname);

        //validate credit card accounts without Nickname, but with PAN and IBAN
        ProviderAccountDTO creditCardAccountWithNoNickname = dataProviderResponse.getAccounts().get(3);
        validateCreditCardAccountWithNoNickname(creditCardAccountWithNoNickname);

        //validate credit card accounts without Nickname and IBAN, but with PAN
        ProviderAccountDTO creditCardAccountWithNoNicknameAndOnlyPAN = dataProviderResponse.getAccounts().get(4);
        validateCreditCardAccountWithNoNicknameAndOnlyPAN(creditCardAccountWithNoNicknameAndOnlyPAN);

    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldRefreshAccessMeansWhenPermisionsIsNotInState(UrlDataProvider provider) throws Exception {
        // given
        ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;

        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(), "my_access_means",
                new Date(),
                new Date());
        AccessMeansState<AccessMeans> token = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of());
        String serializedToken = objectMapper.writeValueAsString(token);
        accessMeans.setAccessMeans(serializedToken);
        accessMeans.setUserId(USER_ID);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO resultAccessMeans = provider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        AccessMeansState<AccessMeans> deserializedOAuthToken = OBJECT_MAPPER.readValue(resultAccessMeans.getAccessMeans(), AccessMeansState.class);

        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessMeans().getAccessToken()).isEqualTo("CrMr1bH8rFR3aJDzrnG8RrjgaXYp");
        assertThat(deserializedOAuthToken.getAccessMeans().getRefreshToken()).isEqualTo("refreshToken");
        assertThat(deserializedOAuthToken.getPermissions()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldRefreshAccessMeansWhenPermissionsAreInState(UrlDataProvider provider) throws Exception {
        // given
        ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;

        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(), "my_access_means",
                new Date(),
                new Date());
        AccessMeans token = new AccessMeans(
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        List<String> permissionsList = Stream.concat(Stream.of(OBReadConsent1Data.PermissionsEnum.READPARTY),
                        DEFAULT_PERMISSIONS.stream())
                .map(OBReadConsent1Data.PermissionsEnum::toString)
                .collect(Collectors.toList());
        AccessMeansState<AccessMeans> accessMeansWithState = new AccessMeansState<>(token, permissionsList);
        String serializedToken = objectMapper.writeValueAsString(accessMeansWithState);
        accessMeans.setAccessMeans(serializedToken);
        accessMeans.setUserId(USER_ID);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO resultAccessMeans = provider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        AccessMeansState<AccessMeans> deserializedOAuthToken = OBJECT_MAPPER.readValue(resultAccessMeans.getAccessMeans(), AccessMeansState.class);

        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessMeans().getAccessToken()).isEqualTo("CrMr1bH8rFR3aJDzrnG8RrjgaXYp");
        assertThat(deserializedOAuthToken.getAccessMeans().getRefreshToken()).isEqualTo("refreshToken");
        assertThat(deserializedOAuthToken.getPermissions()).containsExactlyInAnyOrderElementsOf(List.of(
                "ReadParty",
                "ReadAccountsDetail",
                "ReadBalances",
                "ReadDirectDebits",
                "ReadProducts",
                "ReadStandingOrdersDetail",
                "ReadTransactionsCredits",
                "ReadTransactionsDebits",
                "ReadTransactionsDetail"
        ));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider provider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "asF0cFXAI6bacqdrGRArpwrR6fK6yq";
        String redirectUrl = "https://www.yolt.com/callback?code=" + authorizationCode + "&state=secretState";
        Date _29MinutesFromNow = Date.from(Instant.now().plus(4, ChronoUnit.MINUTES));

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
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
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO newAccessMeans = provider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_29MinutesFromNow).isBefore(newAccessMeans.getExpireTime());
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeansState<AccessMeans> accessMeansState = OBJECT_MAPPER.readValue(newAccessMeans.getAccessMeans(), AccessMeansState.class);
        assertThat(accessMeansState.getAccessMeans().getAccessToken()).isEqualTo("bATusAcZE9sgY0NaAMy2jVqckEGs");
        assertThat(_29MinutesFromNow).isBefore(accessMeansState.getAccessMeans().getExpireTime());
        assertThat(accessMeansState.getPermissions()).containsExactlyInAnyOrderElementsOf(List.of(
                "ReadParty",
                "ReadAccountsDetail",
                "ReadBalances",
                "ReadDirectDebits",
                "ReadProducts",
                "ReadStandingOrdersDetail",
                "ReadTransactionsCredits",
                "ReadTransactionsDebits",
                "ReadTransactionsDetail"));
    }

    private void validateCurrentAccountWithHolderName(ProviderAccountDTO providerAccountDTO, String holderName) {
        validateCurrentAccount(providerAccountDTO);
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo(holderName);
    }

    private void validateCurrentAccountWithoutHolderName(ProviderAccountDTO providerAccountDTO) {
        validateCurrentAccount(providerAccountDTO);
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isNull();
    }

    private void validateCurrentAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("aWgzYIajjaEa_49a30WqJndaislDY_8Z4aPl7Izr");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("1458.16");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("1458.16");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("02011632175602");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getName()).isEqualTo("070131 XXXX1605");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSameElementsAs(getExtendedBalancesForCurrentAccount());
    }

    private void validateCurrentTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(4);

        ProviderTransactionDTO pendingTransaction = transactions.get(0);
        assertThat(pendingTransaction.getAmount()).isEqualTo("7.93");
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(pendingTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(pendingTransaction.getDateTime()).isEqualTo("2020-10-24T01:00+01:00[Europe/London]");
        ExtendedTransactionDTO extendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-10-24T01:00+01:00[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-10-24T01:00+01:00[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-7.93");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Contactless Payment");
        pendingTransaction.validate();

        ProviderTransactionDTO bookedTransaction = transactions.get(1);
        assertThat(bookedTransaction.getAmount()).isEqualTo("9.45");
        assertThat(bookedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(bookedTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(bookedTransaction.getDateTime()).isEqualTo("2020-10-24T01:00+01:00[Europe/London]");
        extendedTransaction = bookedTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-10-24T01:00+01:00[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-10-24T01:00+01:00[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("9.45");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Contactless Payment");
        bookedTransaction.validate();
    }

    private void validateCreditCardAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("AuauT3LMzHwjBb-BqwHupxAe16uS8nJkNC7JmIPN");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("-696.79");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("696.79");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber()).isNull();
        assertThat(providerAccountDTO.getName()).isEqualTo("488386XXXXXX8028");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isEqualTo("485432XXXXXX1023");
        assertThat(providerAccountDTO.getCreditCardData().getAvailableCreditAmount()).isEqualTo("-696.79");

        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSameElementsAs(getExtendedBalancesForCreditCard());
        assertThat(providerAccountDTO.getTransactions()).isEmpty();
    }

    private void validateCreditCardAccountWithNoNickname(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB53 MIIA 0512 3633 18 5404");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getName()).isEqualTo("Nationwide Account");
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isEqualTo("485432XXXXXX1023");
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.IBAN).value("GB53 MIIA 0512 3633 18 5404").build(),
                AccountReferenceDTO.builder().type(AccountReferenceType.MASKED_PAN).value("485432XXXXXX1023").build()
        );
    }

    private void validateCreditCardAccountWithNoPANAndNoNickname(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB53 MIIA 0512 3633 18 5404");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getName()).isEqualTo("Nationwide Account");
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.IBAN).value("GB53 MIIA 0512 3633 18 5404").build()
        );
    }

    private void validateCreditCardAccountWithNoNicknameAndOnlyPAN(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAccountNumber()).isNull();
        assertThat(providerAccountDTO.getName()).isEqualTo("Nationwide Account");
        assertThat(providerAccountDTO.getAccountMaskedIdentification()).isEqualTo("485432XXXXXX1023");
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.MASKED_PAN).value("485432XXXXXX1023").build()
        );
    }

    private List<BalanceDTO> getExtendedBalancesForCreditCard() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("696.79")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T08:58:46.365Z")).withZoneSameInstant(zoneId))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T08:58:46.365Z")).withZoneSameInstant(zoneId))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.AVAILABLE)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("696.79")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T08:58:46.365Z")).withZoneSameInstant(zoneId))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T08:58:46.365Z")).withZoneSameInstant(zoneId))
                .build());
        return balanceList;
    }

    private List<BalanceDTO> getExtendedBalancesForCurrentAccount() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("1458.16")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T10:38:37.325Z")).withZoneSameInstant(zoneId))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T10:38:37.325Z")).withZoneSameInstant(zoneId))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_BOOKED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("1458.16")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T10:38:37.325Z")).withZoneSameInstant(zoneId))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-12-03T10:38:37.325Z")).withZoneSameInstant(zoneId))
                .build());
        return balanceList;

        //ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(balance.getDateTime())).withZoneSameInstant(zoneId)
    }
}
