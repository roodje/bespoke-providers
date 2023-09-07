package com.yolt.providers.openbanking.ais.kbciegroup.ais;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.kbciegroup.KbcIeGroupApp;
import com.yolt.providers.openbanking.ais.kbciegroup.KbcIeGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.kbciegroup.common.KbcIeGroupBaseDataProviderV1;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder.CLIENT_ID_NAME;
import static com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder.CLIENT_SECRET_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in KBC IE group providers.
 * <p>
 * Disclaimer: there is only one bank in the group, but parametrized tests can be useful, when new version of this
 * provider will be implemented. Due to that fact this test class is parametrized, so all providers in group are tested.
 * <p>
 * Covered flows:
 * - register authentication means using autoonboarding
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts, balances, transactions, standing orders, direct debits and parties
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {KbcIeGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("kbciegroup")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/kbciegroup/ais-1.0/happy-flow"}, httpsPort = 0, port = 0)
class KbcIeGroupDataProviderHappyFlowIntegrationTest {

    private Signer signer = new SignerMock();

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Autowired
    @Qualifier("KbcIeDataProviderV1")
    private KbcIeGroupBaseDataProviderV1 kbcIeDataProviderV1;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(kbcIeDataProviderV1);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAuthenticationMeansWithClientIdAndClientSecretForAutoConfigureMeansWhenCorrectDataAreProvided(AutoOnboardingProvider dataProvider) throws IOException, URISyntaxException {
        // given
        String expectedClientId = "someClientId";
        String expectedClientSecret = "someClientSecret";
        Map<String, BasicAuthenticationMean> authenticationMeansBeforeAutoonboarding = new KbcIeGroupSampleAuthenticationMeans().getKbcIeGroupSampleAuthenticationMeansForAutoonboarding();
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setRedirectUrls(List.of("http://redirect1", "http://redirect2"))
                .setAuthenticationMeans(authenticationMeansBeforeAutoonboarding)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> result = dataProvider.autoConfigureMeans(autoOnboardingRequest);

        // then
        assertThat(result).satisfies(authMeansMap -> {
            assertThat(authMeansMap).hasSize(17);
            assertThat(authMeansMap).containsAllEntriesOf(authenticationMeansBeforeAutoonboarding);
            assertThat(authMeansMap).containsEntry(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                    expectedClientId));
            assertThat(authMeansMap).containsEntry(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                    expectedClientSecret));

        });
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepWithConsentIdProperRedirectUrlAndProviderStateForGetLoginInfoWhenCorrectDataAreProvided(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(new KbcIeGroupSampleAuthenticationMeans().getKbcIeGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setState("state")
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback")
                .build();

        // when
        RedirectStep result = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(result.getExternalConsentId()).isEqualTo("CNS19252587RSWHN");
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap()).containsAllEntriesOf(Map.of(
                OAuth.RESPONSE_TYPE, OAuth.CODE,
                OAuth.CLIENT_ID, "someClientId",
                OAuth.STATE, "state",
                OAuth.SCOPE, "openid+accounts",
                OAuth.REDIRECT_URI, "https%3A%2F%2Fwww.yolt.com%2Fcallback"
        ));
        assertThat(uriComponents.getQueryParams().toSingleValueMap()).containsKey("request");
        assertThat(result.getProviderState()).isEqualTo("""
                {"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]}""");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAccessMeansForCreateNewAccessMeansWhenCorrectDataAreProvided(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(new KbcIeGroupSampleAuthenticationMeans().getKbcIeGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setState("state")
                .setProviderState("""
                        {"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]}""")
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback?code=9b892f60-f5b3-11eb-9a03-0242ac130003")
                .setUserId(userId)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThat(result.getStep()).isNull();
        assertThat(result.getAccessMeans()).satisfies(accessMeansDTO -> {
            assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "accessToken":"7531d7a8-7f5e-4bde-ae07-a5c587458ad7""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "refreshToken":"1833b646-0fbf-11ec-82a8-0242ac130003""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "redirectUri":"https://www.yolt.com/callback""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]""");
            assertThat(accessMeansDTO.getExpireTime()).isCloseTo(new Date(), 600000L);
            assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), 5000);
        });
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRenewedAccessMeansForRefreshAccessMeansWhenCorrectDataAreProvided(UrlDataProvider dataProvider) throws TokenInvalidException, IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(new KbcIeGroupSampleAuthenticationMeans().getKbcIeGroupSampleAuthenticationMeansForAis())
                .setAccessMeans(userId, """
                                {"accessMeans":{"created":"2021-09-07T11:07:55.994987300Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"7531d7a8-7f5e-4bde-ae07-a5c587458ad7","refreshToken":"1833b646-0fbf-11ec-82a8-0242ac130003","expireTime":"2021-09-07T11:17:56.426+0000","updated":"2021-09-07T11:07:56.427+0000","redirectUri":"https://www.yolt.com/callback"},"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAccessMeans()).contains("""
                "userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47""");
        assertThat(result.getAccessMeans()).contains("""
                "accessToken":"62442202-0fc9-11ec-82a8-0242ac130003""");
        assertThat(result.getAccessMeans()).contains("""
                "refreshToken":"68e8abfa-0fc9-11ec-82a8-0242ac130003""");
        assertThat(result.getAccessMeans()).contains("""
                "redirectUri":"https://www.yolt.com/callback""");
        assertThat(result.getAccessMeans()).contains("""
                "permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]""");
        assertThat(result.getExpireTime()).isCloseTo(new Date(), 600000L);
        assertThat(result.getUpdated()).isCloseTo(new Date(), 5000);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteUserSiteForOnUserSiteDelete(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("f314bf20-0fcf-11ec-82a8-0242ac130003")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(new KbcIeGroupSampleAuthenticationMeans().getKbcIeGroupSampleAuthenticationMeansForAis())
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }


    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnDataProviderResponseWithAllDataProperlyMappedForFetchDataWhenCorrectDataIsProvided(UrlDataProvider dataProvider) throws IOException, URISyntaxException, TokenInvalidException, ProviderFetchDataException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(userId,
                        """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"0c3da8a6-24ff-11ec-9621-0242ac130002","expireTime":"2021-10-19T11:55:11.525Z","updated":"2021-07-21T11:55:11.525Z","redirectUri":"https://www.yolt.com/callback"},"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]}""",
                        Date.from(Instant.parse("2021-07-21T11:55:11.525Z")),
                        Date.from(Instant.parse("2021-10-19T11:55:11.525Z")))
                .setAuthenticationMeans(new KbcIeGroupSampleAuthenticationMeans().getKbcIeGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setUserId(userId)
                .setTransactionsFetchStartTime(Instant.now().minus(90, ChronoUnit.DAYS))
                .build();

        // when
        DataProviderResponse result = dataProvider.fetchData(urlFetchDataRequest);
        //then
        assertThat(result.getAccounts()).hasSize(2);
        assertThat(result.getAccounts().stream().filter(a -> a.getAccountId().equals("bcd6a21e-24e4-11ec-9621-0242ac130002")).findFirst().get()).satisfies(currentAccountAssertions());
        assertThat(result.getAccounts().stream().filter(a -> a.getAccountId().equals("2b462486-24e5-11ec-9621-0242ac130002")).findFirst().get()).satisfies(creditAccountAssertions());

    }

    private Consumer<ProviderAccountDTO> currentAccountAssertions() {
        return currentAccount -> {
            assertThat(currentAccount).extracting(ProviderAccountDTO::getYoltAccountType,
                    ProviderAccountDTO::getAvailableBalance,
                    ProviderAccountDTO::getCurrentBalance,
                    ProviderAccountDTO::getAccountId,
                    ProviderAccountDTO::getName,
                    ProviderAccountDTO::getCurrency
            ).contains(AccountType.CURRENT_ACCOUNT,
                    new BigDecimal("121.55"),
                    new BigDecimal("123.88"),
                    "bcd6a21e-24e4-11ec-9621-0242ac130002",
                    "Current account nickname",
                    CurrencyCode.EUR);
            assertThat(currentAccount.getAccountNumber()).extracting(ProviderAccountNumberDTO::getScheme,
                    ProviderAccountNumberDTO::getIdentification
            ).contains(
                    ProviderAccountNumberDTO.Scheme.IBAN,
                    "IE14BOFI900017611258"
            );
            assertThat(currentAccount.getExtendedAccount()).extracting(ExtendedAccountDTO::getResourceId,
                    ExtendedAccountDTO::getCurrency,
                    ExtendedAccountDTO::getName
            ).contains("bcd6a21e-24e4-11ec-9621-0242ac130002",
                    CurrencyCode.EUR,
                    "Current account nickname");
            assertThat(currentAccount.getExtendedAccount().getAccountReferences().get(0)).extracting(AccountReferenceDTO::getType,
                    AccountReferenceDTO::getValue
            ).contains(
                    AccountReferenceType.IBAN,
                    "IE14BOFI900017611258"
            );
            assertThat(currentAccount.getDirectDebits()).isEmpty();
            assertThat(currentAccount.getStandingOrders()).isEmpty();
            assertThat(currentAccount.getTransactions()).hasSize(4);
            //booked transaction assert
            ProviderTransactionDTO bookedTransaction = currentAccount.getTransactions().stream().filter(t -> "C475869".equals(t.getExternalId())).findFirst().get();
            assertThat(bookedTransaction).extracting(ProviderTransactionDTO::getAmount,
                    ProviderTransactionDTO::getStatus,
                    ProviderTransactionDTO::getType,
                    ProviderTransactionDTO::getDateTime
            ).contains(
                    new BigDecimal("123"),
                    TransactionStatus.BOOKED,
                    ProviderTransactionType.CREDIT,
                    ZonedDateTime.of(LocalDateTime.of(2020, 6, 10, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(bookedTransaction.getExtendedTransaction()).extracting(
                    ExtendedTransactionDTO::getStatus,
                    ExtendedTransactionDTO::getEntryReference,
                    ExtendedTransactionDTO::getRemittanceInformationUnstructured,
                    ExtendedTransactionDTO::getBookingDate,
                    ExtendedTransactionDTO::getValueDate
            ).contains(
                    TransactionStatus.BOOKED,
                    "OP/CG Direct Debit10C561144X7412589",
                    "Cheque Lodgement",
                    ZonedDateTime.of(LocalDateTime.of(2020, 6, 10, 0, 0, 0), ZoneId.of("Europe/Dublin")),
                    ZonedDateTime.of(LocalDateTime.of(2020, 6, 10, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(bookedTransaction.getExtendedTransaction().getTransactionAmount()).extracting(
                    BalanceAmountDTO::getCurrency,
                    BalanceAmountDTO::getAmount
            ).contains(
                    CurrencyCode.EUR,
                    new BigDecimal("123")
            );
            //pending transaction assert
            ProviderTransactionDTO pendingTransaction = currentAccount.getTransactions().stream().filter(t -> t.getAmount().compareTo(new BigDecimal("112.50")) == 0).findFirst().get();
            assertThat(pendingTransaction).extracting(ProviderTransactionDTO::getStatus,
                    ProviderTransactionDTO::getType,
                    ProviderTransactionDTO::getDateTime
            ).contains(TransactionStatus.PENDING,
                    ProviderTransactionType.DEBIT,
                    ZonedDateTime.of(LocalDateTime.of(2020, 4, 11, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(pendingTransaction.getExtendedTransaction()).extracting(
                    ExtendedTransactionDTO::getStatus,
                    ExtendedTransactionDTO::getRemittanceInformationUnstructured,
                    ExtendedTransactionDTO::getBookingDate,
                    ExtendedTransactionDTO::getValueDate
            ).contains(
                    TransactionStatus.PENDING,
                    "POS Centra",
                    ZonedDateTime.of(LocalDateTime.of(2020, 4, 11, 0, 0, 0), ZoneId.of("Europe/Dublin")),
                    ZonedDateTime.of(LocalDateTime.of(2020, 4, 11, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(pendingTransaction.getExtendedTransaction().getTransactionAmount()).extracting(
                    BalanceAmountDTO::getCurrency,
                    BalanceAmountDTO::getAmount
            ).contains(
                    CurrencyCode.EUR,
                    new BigDecimal("-112.50")
            );
        };
    }

    private Consumer<ProviderAccountDTO> creditAccountAssertions() {
        return currentAccount -> {
            assertThat(currentAccount).extracting(ProviderAccountDTO::getYoltAccountType,
                    ProviderAccountDTO::getAvailableBalance,
                    ProviderAccountDTO::getCurrentBalance,
                    ProviderAccountDTO::getAccountId,
                    ProviderAccountDTO::getName,
                    ProviderAccountDTO::getCurrency,
                    ProviderAccountDTO::getAccountMaskedIdentification
            ).contains(AccountType.CREDIT_CARD,
                    new BigDecimal("5001.45"),
                    new BigDecimal("5643.17"),
                    "2b462486-24e5-11ec-9621-0242ac130002",
                    "Credit card nickname",
                    CurrencyCode.EUR,
                    "IE04BOFI900017949289");
            assertThat(currentAccount.getCreditCardData().getAvailableCreditAmount()).isEqualTo("5001.45");
            assertThat(currentAccount.getExtendedAccount()).extracting(ExtendedAccountDTO::getResourceId,
                    ExtendedAccountDTO::getCurrency,
                    ExtendedAccountDTO::getName
            ).contains("2b462486-24e5-11ec-9621-0242ac130002",
                    CurrencyCode.EUR,
                    "Credit card nickname");
            assertThat(currentAccount.getExtendedAccount().getAccountReferences().get(0)).extracting(AccountReferenceDTO::getType,
                    AccountReferenceDTO::getValue
            ).contains(
                    AccountReferenceType.MASKED_PAN,
                    "IE04BOFI900017949289"
            );
            assertThat(currentAccount.getDirectDebits()).isEmpty();
            assertThat(currentAccount.getStandingOrders()).isEmpty();
            assertThat(currentAccount.getTransactions()).hasSize(2);
            //booked transaction assert
            ProviderTransactionDTO bookedTransaction = currentAccount.getTransactions().stream().filter(t -> "C475869".equals(t.getExternalId())).findFirst().get();
            assertThat(bookedTransaction).extracting(ProviderTransactionDTO::getAmount,
                    ProviderTransactionDTO::getStatus,
                    ProviderTransactionDTO::getType,
                    ProviderTransactionDTO::getDateTime
            ).contains(
                    new BigDecimal("88.98"),
                    TransactionStatus.BOOKED,
                    ProviderTransactionType.CREDIT,
                    ZonedDateTime.of(LocalDateTime.of(2020, 6, 10, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(bookedTransaction.getExtendedTransaction()).extracting(
                    ExtendedTransactionDTO::getStatus,
                    ExtendedTransactionDTO::getEntryReference,
                    ExtendedTransactionDTO::getRemittanceInformationUnstructured,
                    ExtendedTransactionDTO::getBookingDate,
                    ExtendedTransactionDTO::getValueDate
            ).contains(
                    TransactionStatus.BOOKED,
                    "OP/CG Direct Debit10C561144X7412589",
                    "Cheque Lodgement",
                    ZonedDateTime.of(LocalDateTime.of(2020, 6, 10, 0, 0, 0), ZoneId.of("Europe/Dublin")),
                    ZonedDateTime.of(LocalDateTime.of(2020, 6, 10, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(bookedTransaction.getExtendedTransaction().getTransactionAmount()).extracting(
                    BalanceAmountDTO::getCurrency,
                    BalanceAmountDTO::getAmount
            ).contains(
                    CurrencyCode.EUR,
                    new BigDecimal("88.98")
            );
            //pending transaction assert
            ProviderTransactionDTO pendingTransaction = currentAccount.getTransactions().stream().filter(t -> t.getAmount().compareTo(new BigDecimal("134.98")) == 0).findFirst().get();
            assertThat(pendingTransaction).extracting(ProviderTransactionDTO::getStatus,
                    ProviderTransactionDTO::getType,
                    ProviderTransactionDTO::getDateTime
            ).contains(TransactionStatus.PENDING,
                    ProviderTransactionType.DEBIT,
                    ZonedDateTime.of(LocalDateTime.of(2020, 5, 12, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(pendingTransaction.getExtendedTransaction()).extracting(
                    ExtendedTransactionDTO::getStatus,
                    ExtendedTransactionDTO::getRemittanceInformationUnstructured,
                    ExtendedTransactionDTO::getBookingDate,
                    ExtendedTransactionDTO::getValueDate
            ).contains(
                    TransactionStatus.PENDING,
                    "POS Tesco",
                    ZonedDateTime.of(LocalDateTime.of(2020, 5, 12, 0, 0, 0), ZoneId.of("Europe/Dublin")),
                    ZonedDateTime.of(LocalDateTime.of(2020, 5, 12, 0, 0, 0), ZoneId.of("Europe/Dublin"))
            );
            assertThat(pendingTransaction.getExtendedTransaction().getTransactionAmount()).extracting(
                    BalanceAmountDTO::getCurrency,
                    BalanceAmountDTO::getAmount
            ).contains(
                    CurrencyCode.EUR,
                    new BigDecimal("-134.98")
            );
        };
    }

}
