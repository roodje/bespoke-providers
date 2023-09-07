package com.yolt.providers.openbanking.ais.permanenttsbgroup.ais;

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
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupApp;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.PermanentTsbGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.PermanentTsbGroupBaseDataProviderV1;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.auth.PermanentTsbGroupAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.VirginMoney2JwsSigningResult;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * This test contains all happy flows occurring in Permanent TSB group providers.
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
@SpringBootTest(classes = {PermanentTsbGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("permanenttsbgroup")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/permanenttsbgroup/ais-3.1/happy-flow"}, httpsPort = 0, port = 0)
class PermanentTsbGroupDataProviderHappyFlowIntegrationTest {

    @Mock
    private Signer signer;

    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff679");

    @Autowired
    @Qualifier("PermanentTsbDataProviderV1")
    private PermanentTsbGroupBaseDataProviderV1 permanentTsbDataProviderV1;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(permanentTsbDataProviderV1);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnClientIdAndClientSecretTypedAuthMeansForGetAutoConfiguredMeans(AutoOnboardingProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> result = dataProvider.getAutoConfiguredMeans();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                PermanentTsbGroupAuthMeansBuilder.CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING,
                PermanentTsbGroupAuthMeansBuilder.CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING
        ));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAuthenticationMeansWithClientIdAndClientSecretForAutoConfigureMeansWhenCorrectDataAreProvided(AutoOnboardingProvider dataProvider) throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAutoonboarding();
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setRedirectUrls(List.of("http://redirect1", "http://redirect2"))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS))
                .build();

        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(new PermanentTsbGroupJwsSigningResult());

        // when
        Map<String, BasicAuthenticationMean> result = dataProvider.autoConfigureMeans(autoOnboardingRequest);

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepWithConsentIdProperRedirectUrlAndProviderStateForGetLoginInfoWhenCorrectDataAreProvided(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setState("022a271a-7ccb-443f-bd83-56a5e1066bdf")
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback")
                .build();

        given(signer.sign(any(JsonWebSignature.class), any(UUID.class), any(SignatureAlgorithm.class)))
                .willReturn(new VirginMoney2JwsSigningResult());

        // when
        RedirectStep result = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(result.getExternalConsentId()).isEqualTo("43fcafe3-332f-4578-802c-fe6d21faca12");
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(result.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                OAuth.RESPONSE_TYPE, "code+id_token",
                OAuth.CLIENT_ID, "someClientId",
                OAuth.STATE, "022a271a-7ccb-443f-bd83-56a5e1066bdf",
                OAuth.SCOPE, "openid+accounts",
                OAuth.NONCE, "022a271a-7ccb-443f-bd83-56a5e1066bdf",
                OAuth.REDIRECT_URI, "https%3A%2F%2Fwww.yolt.com%2Fcallback",
                "request", "V2hhdCBoYXRoIGdvZCB3cm91Z2h0ID8%3D..QnkgR2VvcmdlLCBzaGUncyBnb3QgaXQhIEJ5IEdlb3JnZSBzaGUncyBnb3QgaXQhIE5vdyBvbmNlIGFnYWluLCB3aGVyZSBkb2VzIGl0IHJhaW4"
        ));
        assertThat(result.getProviderState()).isEqualTo("""
                {"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail","ReadAccountsBasic","ReadScheduledPaymentsBasic","ReadScheduledPaymentsDetail","ReadStandingOrdersBasic","ReadStatementsBasic","ReadStatementsDetail","ReadTransactionsBasic"]}""");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAccessMeansForCreateNewAccessMeansWhenCorrectDataAreProvided(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis())
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
                    "redirectUri":"https://www.yolt.com/callback""");
            assertThat(accessMeansDTO.getAccessMeans()).contains("""
                    "permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]""");
            assertThat(accessMeansDTO.getExpireTime()).isCloseTo(new Date(), 7776000000L);
            assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), 5000);
        });
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowTokenInvalidExceptionToAskUserForPerformingConsentStepForRefreshAccessMeans(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        AccessMeans oAuthToken = new AccessMeans(UUID.randomUUID(), "accessToken", "refreshToken", new Date(), new Date(), "https://www.yolt.com/callback");
        String serializedOAuthToken = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(userId, serializedOAuthToken, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest))
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Permanent TSB Group does not support refresh token. An access token is simply valid for 90 days. If expired, the user needs to relogin");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteUserSiteForOnUserSiteDelete(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("43fcafe3-332f-4578-802c-fe6d21faca12")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis())
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnDataProviderResponseWithAllDataProperlyMappedForFetchDataWhenCorrectDataAreProvided(UrlDataProvider dataProvider) throws IOException, URISyntaxException, TokenInvalidException, ProviderFetchDataException {
        // given
        UUID userId = UUID.fromString("b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47");
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(userId,
                        """
                                {"accessMeans":{"created":"2021-07-21T11:55:10.414419500Z","userId":"b6bb3b6c-4c78-4a5c-a3e6-f2b85db89d47","accessToken":"7531d7a8-7f5e-4bde-ae07-a5c587458ad7","expireTime":"2021-10-19T11:55:11.525+0000","updated":"2021-07-21T11:55:11.525+0000","redirectUri":"https://www.yolt.com/callback"},"permissions":["ReadAccountsDetail","ReadBalances","ReadPAN","ReadDirectDebits","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail","ReadStandingOrdersDetail"]}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plusSeconds(60)))
                .setAuthenticationMeans(new PermanentTsbGroupSampleAuthenticationMeans().getPermanentTsbGroupSampleAuthenticationMeansForAis())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setUserId(userId)
                .setTransactionsFetchStartTime(Instant.now().minus(90, ChronoUnit.DAYS))
                .build();

        // when
        DataProviderResponse result = dataProvider.fetchData(urlFetchDataRequest);

        // then
        assertThat(result.getAccounts()).hasSize(3);
        assertThat(findAccount(result.getAccounts(), "E179385A-3774-499C-8D61-142783AB943C")).satisfies(currentAccountAssertions());
        assertThat(findAccount(result.getAccounts(), "EC26D95F-6DDC-44E3-82C1-BA1145D7CEA6")).satisfies(savingsAccountAssertions());
        assertThat(findAccount(result.getAccounts(), "4339903A-0493-43CA-A855-EDAF40CD2835")).satisfies(creditCardAccountAssertions());
    }

    private ProviderAccountDTO findAccount(List<ProviderAccountDTO> accounts, String accountId) {
        return accounts.stream().filter(acc -> acc.getAccountId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    private Consumer<ProviderAccountDTO> currentAccountAssertions() {
        return currentAccount -> {
            assertThat(currentAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(currentAccount.getCurrentBalance()).isEqualTo("217.24");
            assertThat(currentAccount.getAvailableBalance()).isEqualTo("217.24");
            assertThat(currentAccount.getAccountId()).isEqualTo("E179385A-3774-499C-8D61-142783AB943C");
            assertThat(currentAccount.getAccountNumber()).satisfies(accountNumber -> {
                assertThat(accountNumber.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
                assertThat(accountNumber.getIdentification()).isEqualTo("IE79IPBS99066012344321");
                assertThat(accountNumber.getHolderName()).isNull();
            });
            assertThat(currentAccount.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), byLessThan(1, ChronoUnit.MINUTES));
            assertThat(currentAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(currentAccount.getName()).isEqualTo("Joe Bloggs Reg Saver");
            assertThat(currentAccount.getTransactions()).first().satisfies(firstTransactionAssertions());
            assertThat(currentAccount.getDirectDebits()).isEmpty();
            assertThat(currentAccount.getStandingOrders()).isEmpty();
            assertThat(currentAccount.getExtendedAccount()).satisfies(extendedAccountAssertions());
        };
    }

    private Consumer<ProviderAccountDTO> savingsAccountAssertions() {
        return savingsAccount -> {
            assertThat(savingsAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(savingsAccount.getCurrentBalance()).isEqualTo("105");
            assertThat(savingsAccount.getAvailableBalance()).isEqualTo("424.75");
            assertThat(savingsAccount.getAccountId()).isEqualTo("EC26D95F-6DDC-44E3-82C1-BA1145D7CEA6");
            assertThat(savingsAccount.getAccountNumber()).satisfies(accountNumber -> {
                assertThat(accountNumber.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
                assertThat(accountNumber.getIdentification()).isEqualTo("IE79IPBS99066012344323");
                assertThat(accountNumber.getHolderName()).isNull();
            });
            assertThat(savingsAccount.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), byLessThan(1, ChronoUnit.MINUTES));
            assertThat(savingsAccount.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
            assertThat(savingsAccount.getName()).isEqualTo("John's Savings Account");
        };
    }

    private Consumer<ProviderAccountDTO> creditCardAccountAssertions() {
        return creditCardAccount -> {
            assertThat(creditCardAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(creditCardAccount.getCurrentBalance()).isEqualTo("2262.17");
            assertThat(creditCardAccount.getAvailableBalance()).isEqualTo("1000");
            assertThat(creditCardAccount.getAccountId()).isEqualTo("4339903A-0493-43CA-A855-EDAF40CD2835");
            assertThat(creditCardAccount.getAccountNumber()).isNull();
            assertThat(creditCardAccount.getAccountMaskedIdentification()).isEqualTo("xxxx 1881");
            assertThat(creditCardAccount.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), byLessThan(1, ChronoUnit.MINUTES));
            assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
            assertThat(creditCardAccount.getName()).isEqualTo("Paul's Credit Card A/C");
        };
    }

    private Consumer<ProviderTransactionDTO> firstTransactionAssertions() {
        return transaction -> {
            assertThat(transaction.getAmount()).isEqualTo("117.50");
            assertThat(transaction.getDescription()).isEqualTo("POS Centra");
            assertThat(transaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
            assertThat(transaction.getDateTime()).isEqualTo("2020-04-11T00:00+01:00[Europe/Dublin]");
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
            assertThat(transaction.getExtendedTransaction()).satisfies(extendedTransactionAssertions());
        };
    }

    private Consumer<ExtendedTransactionDTO> extendedTransactionAssertions() {
        return extendedTransaction -> {
            assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-04-11T00:00+01:00[Europe/Dublin]");
            assertThat(extendedTransaction.getTransactionAmount()).satisfies(amount -> {
                assertThat(amount.getCurrency()).isEqualTo(CurrencyCode.EUR);
                assertThat(amount.getAmount()).isEqualTo("-117.50");
            });
            assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
            assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("POS Centra");
            assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-04-11T00:00+01:00[Europe/Dublin]");
            assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-04-11T00:00+01:00[Europe/Dublin]");
        };
    }

    private Consumer<ExtendedAccountDTO> extendedAccountAssertions() {
        return extendedAccount -> {
            assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(extendedAccount.getName()).isEqualTo("Joe Bloggs Reg Saver");
            assertThat(extendedAccount.getStatus()).isEqualTo(Status.ENABLED);
            assertThat(findBalance(extendedAccount.getBalances(), BalanceType.INFORMATION)).satisfies(balance -> {
                assertThat(balance.getBalanceAmount()).satisfies(amount -> {
                    assertThat(amount.getAmount()).isEqualTo("217.24");
                    assertThat(amount.getCurrency()).isEqualTo(CurrencyCode.EUR);
                });
                assertThat(balance.getBalanceType()).isEqualTo(BalanceType.INFORMATION);
                assertThat(balance.getLastChangeDateTime()).isEqualTo("2018-10-18T00:00+01:00[Europe/Dublin]");
                assertThat(balance.getReferenceDate()).isEqualTo("2018-10-18T00:00+01:00[Europe/Dublin]");
            });
            assertThat(findBalance(extendedAccount.getBalances(), BalanceType.INTERIM_BOOKED)).satisfies(balance -> {
                assertThat(balance.getBalanceAmount()).satisfies(amount -> {
                    assertThat(amount.getAmount()).isEqualTo("217.24");
                    assertThat(amount.getCurrency()).isEqualTo(CurrencyCode.EUR);
                });
                assertThat(balance.getBalanceType()).isEqualTo(BalanceType.INTERIM_BOOKED);
                assertThat(balance.getLastChangeDateTime()).isEqualTo("2018-10-18T00:00+01:00[Europe/Dublin]");
                assertThat(balance.getReferenceDate()).isEqualTo("2018-10-18T00:00+01:00[Europe/Dublin]");
            });
            assertThat(extendedAccount.getBic()).isEqualTo("IPBSIE2D");
            assertThat(extendedAccount.getResourceId()).isEqualTo("E179385A-3774-499C-8D61-142783AB943C");
            assertThat(extendedAccount.getUsage()).isEqualTo(UsageType.PRIVATE);
        };
    }

    private BalanceDTO findBalance(List<BalanceDTO> balances, BalanceType type) {
        return balances.stream().filter(balance -> balance.getBalanceType() == type)
                .findFirst()
                .orElse(null);
    }
}
