package com.yolt.providers.openbanking.ais.lloydsbankinggroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsGroupApp;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.utils.JwtHelper;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.utils.UriHelper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;

/**
 * This test contains all happy flows occurring in LBG group group providers.
 * <p>
 * Disclaimer: most providers in LBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised, so all providers in group are tested.
 * <p>
 * Covered flows:
 * - updating authentication means using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders, direct debits
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {LloydsGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lloydsgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lloydsbankinggroup/ais/happy-flow/", httpsPort = 0, port = 0)
public class LloydsGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private RestTemplateManagerMock restTemplateManagerMock;
    @Autowired
    @Qualifier("BankOfScotlandDataProviderV10")
    private GenericBaseDataProvider bankOfScotlandDataProviderV10;
    @Autowired
    @Qualifier("BankOfScotlandCorpoDataProviderV8")
    private GenericBaseDataProvider bankOfScotlandCorpoDataProviderV8;
    @Autowired
    @Qualifier("HalifaxDataProviderV10")
    private GenericBaseDataProvider halifaxDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankDataProviderV10")
    private GenericBaseDataProvider lloydsBankDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankCorpoDataProviderV8")
    private GenericBaseDataProvider lloydsBankCorpoDataProviderV8;
    @Autowired
    @Qualifier("MbnaCreditCardDataProviderV6")
    private GenericBaseDataProvider mbnaCreditCardDataProviderV6;
    private Signer signer;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private AccessMeans token;

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new LloydsSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "c554a9ef-47c1-4b4e-a77f-2ad770d69748");
        token = new AccessMeans();
        token.setCreated(Instant.now());
        token.setAccessToken("accessToken");
        token.setExpireTime(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithExpectedAudience")
    public void shouldReturnFAPICompliantRedirectUrl(UrlDataProvider dataProvider, String expectedAudience) throws MalformedClaimException {
        // given
        String expectedState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(expectedState)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(3600);

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("363ca7c1-9d03-4876-8766-ddefc9fd2d76");
        String redirectUrl = loginInfo.getRedirectUrl();
        assertThat(redirectUrl).isNotEmpty();

        Map<String, String> queryParams = UriHelper.extractQueryParams(redirectUrl);
        JwtClaims jwtClaims = JwtHelper.parseJwtClaims(queryParams.get("request"));

        assertThat(jwtClaims.getIssuer()).isEqualTo("a4f99159-cb97-4667-b82e-553e8ad8a632");
        assertThat(jwtClaims.getAudience()).containsOnly(expectedAudience);
        assertThat(jwtClaims.getExpirationTime()).isNotNull();
        assertThat(jwtClaims.getExpirationTime().getValue()).isGreaterThanOrEqualTo(expirationDate.getValue());
        assertThat(queryParams)
                .containsOnlyKeys("nonce", "response_type", "client_id", "scope", "state", "redirect_uri", "request")
                .hasEntrySatisfying("nonce", nonce ->
                        assertThat(nonce)
                                .isEqualTo(expectedState.substring(0, 8))
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "nonce")))
                .hasEntrySatisfying("response_type", responseType ->
                        assertThat(responseType)
                                .isEqualTo("code+id_token")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "response_type").replace(" ", "+")))
                .hasEntrySatisfying("client_id", clientId ->
                        assertThat(clientId)
                                .isEqualTo("a4f99159-cb97-4667-b82e-553e8ad8a632")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "client_id")))
                .hasEntrySatisfying("scope", scope ->
                        assertThat(scope)
                                .isEqualTo("openid+accounts")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "scope").replace(" ", "+")))
                .hasEntrySatisfying("state", state ->
                        assertThat(state)
                                .isEqualTo(expectedState)
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "state")))
                .hasEntrySatisfying("redirect_uri", redirectUri ->
                        assertThat(redirectUri)
                                .isEqualTo("http%3A%2F%2Fyolt.com%2Fidentifier")
                                .isEqualTo(JwtHelper.extractStringClaim(jwtClaims, "redirect_uri").replace(":", "%3A").replace("/", "%2F")));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCorrectlyFetchData(UrlDataProvider dataProvider) throws Exception {
        // given
        String serializedAccessMeans = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setTransactionsFetchStartTime(Instant.now())
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        // Verify CurrentAccount
        ProviderAccountDTO currentAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "80496010738761");
        assertThat(currentAccount.getName()).isEqualTo("80496010738761");
        assertThat(currentAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(currentAccount.getCurrentBalance()).isEqualTo("-1.00");
        assertThat(currentAccount.getAvailableBalance()).isEqualTo("9.00");
        assertThat(currentAccount.getClosed()).isNull();
        assertThat(currentAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Stand Order for CurrentAccount
        assertThat(currentAccount.getStandingOrders()).hasSize(1);
        assertThat(currentAccount.getStandingOrders().get(0).getDescription()).isEqualTo("Towbar Club 2 - We Love Towbars");
        assertThat(currentAccount.getStandingOrders().get(0).getFrequency()).isEqualTo(Period.ofDays(1));
        assertThat(currentAccount.getStandingOrders().get(0).getNextPaymentAmount()).isEqualTo("0.56");
        assertThat(currentAccount.getStandingOrders().get(0).getCounterParty().getIdentification()).isEqualTo("80200112345678");

        // Verify Direct Debit for CurrentAccount
        assertThat(currentAccount.getDirectDebits()).hasSize(1);
        assertThat(currentAccount.getDirectDebits().get(0).getDescription()).isEqualTo("Towbar Club 3 - We Love Towbars");
        assertThat(currentAccount.getDirectDebits().get(0).isDirectDebitStatus()).isTrue();
        assertThat(currentAccount.getDirectDebits().get(0).getPreviousPaymentAmount()).isEqualTo("0.57");

        // Verify SavingsAccount
        ProviderAccountDTO savingsAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "80496010738762");
        assertThat(savingsAccount.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
        assertThat(savingsAccount.getAvailableBalance()).isEqualTo("9.00");
        assertThat(savingsAccount.getCurrentBalance()).isEqualTo("-1.00");
        assertThat(savingsAccount.getName()).isEqualTo("Savings Account");
        assertThat(savingsAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(savingsAccount.getCreditCardData()).isNull();
        assertThat(savingsAccount.getAccountNumber()).isNotNull();
        assertThat(savingsAccount.getTransactions()).hasSize(7);

        // Verify CreditCardAccount
        ProviderAccountDTO creditCardAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "80496010738763");
        assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(creditCardAccount.getAvailableBalance()).isEqualTo("9.00");
        assertThat(creditCardAccount.getCurrentBalance()).isEqualTo("-1.00");
        assertThat(creditCardAccount.getName()).isEqualTo("Credit Card");
        assertThat(creditCardAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(creditCardAccount.getCreditCardData()).isNotNull();
        assertThat(creditCardAccount.getAccountNumber()).isNotNull();
        assertThat(creditCardAccount.getTransactions()).hasSize(8);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    // This test exists since there is a bug in Lloyds' code that makes us unable to use their accountId as id.
    public void shouldFetchDataWhenAccountNumberIdentificationIsUsedForAccountId(UrlDataProvider dataProvider) throws Exception {
        // given
        String serializedAccessMeans = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        List<String> accountIds = dataProviderResponse.getAccounts().stream().map(ProviderAccountDTO::getAccountId).collect(toList());
        assertThat(accountIds).containsExactly("80496010738761", "80496010738762", "80496010738763", "80496010738764");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";

        final String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + authorizationCode + "&state=secretState";
        Date _89DaysFromNow = Date.from(Instant.now().plus(89, ChronoUnit.DAYS));

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_89DaysFromNow).isBefore(newAccessMeans.getExpireTime());
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans accessMeans = OpenBankingTestObjectMapper.INSTANCE.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("gktvoeyJhbGciOiJSUzI1NiIsImtpZCI6InE0LTIwMTctbTEtQk9TIn0.eyJpc3MiOiJsbG95ZHMiLCJwcml2YXRlIjoiQUFJa01ETTVOREptWlRVdE9HTmlNaTAwTnpWbUxXSXdNVEl0TkRneVpqTTBaVEV4WXpJNTIxc0xZVE5TcF9Cc2ZsQnZVSHRubERSNjdUa3hkQ1VBM0ZRenE1QWlQVjlXelljRlB3d09BUTY2THFnMVRWWXY5eGFFOVZHSmY1eWl4c1pvR29VSkJjaENVXzZtQk5BaDd3TGxmUHl4Z3d0eGRaX3NFSEFGT2Jha1gtc2ZOQ1V3LWk0QVhtN2xUT1J0QlBjdEx4UFpEM1BJLU9TTUUzQllpeXR6VFJUcGZtT2ZxOEt2TmN1bXVMV1UtdC0ycHpsbjdZOHhIaGlCTnhMQ0lhUk5XZmxuWllVcURvVEkyOEk3Z0FVeUhLbTlueFNZcXY0VENrU004OHB6dVhVY2Y3UlJ1RDI0S1Q4M2wxZUxoR1F3Slc3TmdPZWZWRWtzMzRtYWlrWXhUMldPajBpUlhXWmRBampZZnNjQ2M4RHREYUYwOFFSMUJLRFJlNHZoSkdwQUJpZW9RZkthLWJIRGxBMjJvb1F2aTUweEwzRG5vdnlkcDBIV25lc0ZoNHlqbWlSZ1dlWk9lYWRFdXhqNXdKMnAxeHBVNkJoWEg1VFByRGdOUlJaZUJ3WnJUZFJ2NWJPUFNIaGI3SXNBOUF5NC01bU41SzJsQ0k3SkVobHJVN2RLeUlhdW9nIn0.c7dc4sSJvIjcYbxgiMp886bvezAwkLuZXEnQbfUhdVzL2i1mKts5PeDBsYAFErg6zyT1Q9aBzFsMgez7G3KarDJrBx1Wsd6mIKxnENbYkCmBtHYzxGkRmLciL4qB60IAMhHLSz9XCNz-OfHYEUffeY9kwwpaW2ibA7av5Q2ZB7PEmSa41bXiuTZal7MMCGRZzWfJppSA_rLhl-xCuIgyGdNVY0uF4wKqD6GWjqTEry3era3NoNvHatde1gFIUqkYiMw3XSv5IfulT-_YLanGHKe_S6JsGijAPhLT5DRz5V0ijXPzCwe2-wDHS95dgrGzSFk1QE23AeVwsWXwkNG2vQ");
        assertThat(_89DaysFromNow).isBefore(accessMeans.getExpireTime());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeansWithRefreshToken(UrlDataProvider dataProvider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "AUTHORIZATION-CODE-FOR-RESPONSE-WITH-REFRESH-TOKEN";

        final String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0#code=" + authorizationCode + "&state=secretState";
        Date _89DaysFromNow = Date.from(Instant.now().plus(89, ChronoUnit.DAYS));

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_89DaysFromNow).isBefore(newAccessMeans.getExpireTime());
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans accessMeans = OpenBankingTestObjectMapper.INSTANCE.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("access-token-received-from-authorization-code-grant-type");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("refresh-token-received-from-authorization-code-grant-type");
        assertThat(_89DaysFromNow).isBefore(accessMeans.getExpireTime());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowTokenInvalidExceptionIfRefreshTokenIsNonPresentInAccessMeans(UrlDataProvider dataProvider) {
        //given
        UUID userID = UUID.randomUUID();
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAccessMeans(new AccessMeansDTO(userID,
                        """
                                {"created":"2022-09-01T07:29:11.298555900Z","userId":"fac4c38c-28f7-4281-b165-ba01545bc3ec","accessToken":"gktvoeyJhbGciOiJSUzI1NiIsImtpZCI6InE0LTIwMTctbTEtQk9TIn0.eyJpc3MiOiJsbG95ZHMiLCJwcml2YXRlIjoiQUFJa01ETTVOREptWlRVdE9HTmlNaTAwTnpWbUxXSXdNVEl0TkRneVpqTTBaVEV4WXpJNTIxc0xZVE5TcF9Cc2ZsQnZVSHRubERSNjdUa3hkQ1VBM0ZRenE1QWlQVjlXelljRlB3d09BUTY2THFnMVRWWXY5eGFFOVZHSmY1eWl4c1pvR29VSkJjaENVXzZtQk5BaDd3TGxmUHl4Z3d0eGRaX3NFSEFGT2Jha1gtc2ZOQ1V3LWk0QVhtN2xUT1J0QlBjdEx4UFpEM1BJLU9TTUUzQllpeXR6VFJUcGZtT2ZxOEt2TmN1bXVMV1UtdC0ycHpsbjdZOHhIaGlCTnhMQ0lhUk5XZmxuWllVcURvVEkyOEk3Z0FVeUhLbTlueFNZcXY0VENrU004OHB6dVhVY2Y3UlJ1RDI0S1Q4M2wxZUxoR1F3Slc3TmdPZWZWRWtzMzRtYWlrWXhUMldPajBpUlhXWmRBampZZnNjQ2M4RHREYUYwOFFSMUJLRFJlNHZoSkdwQUJpZW9RZkthLWJIRGxBMjJvb1F2aTUweEwzRG5vdnlkcDBIV25lc0ZoNHlqbWlSZ1dlWk9lYWRFdXhqNXdKMnAxeHBVNkJoWEg1VFByRGdOUlJaZUJ3WnJUZFJ2NWJPUFNIaGI3SXNBOUF5NC01bU41SzJsQ0k3SkVobHJVN2RLeUlhdW9nIn0.c7dc4sSJvIjcYbxgiMp886bvezAwkLuZXEnQbfUhdVzL2i1mKts5PeDBsYAFErg6zyT1Q9aBzFsMgez7G3KarDJrBx1Wsd6mIKxnENbYkCmBtHYzxGkRmLciL4qB60IAMhHLSz9XCNz-OfHYEUffeY9kwwpaW2ibA7av5Q2ZB7PEmSa41bXiuTZal7MMCGRZzWfJppSA_rLhl-xCuIgyGdNVY0uF4wKqD6GWjqTEry3era3NoNvHatde1gFIUqkYiMw3XSv5IfulT-_YLanGHKe_S6JsGijAPhLT5DRz5V0ijXPzCwe2-wDHS95dgrGzSFk1QE23AeVwsWXwkNG2vQ","expireTime":"2022-11-30T07:29:12.153+0000","updated":"2022-09-01T07:29:12.153+0000","redirectUri":"https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plus(89, ChronoUnit.DAYS))))
                .build();

        //when
        ThrowableAssert.ThrowingCallable call = () -> dataProvider.refreshAccessMeans(request);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshAccessMeansIfRefreshTokenIsPresentInAccessMeans(UrlDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException {
        //given
        UUID userId = UUID.randomUUID();
        Date _89DaysFromNow = Date.from(Instant.now().plus(89, ChronoUnit.DAYS));
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAccessMeans(new AccessMeansDTO(userId,
                        """
                                {"created":"2022-09-01T07:29:11.298555900Z","userId":"fac4c38c-28f7-4281-b165-ba01545bc3ec","accessToken":"access-token-should-be-refreshed","refreshToken":"refresh-token-which-should-be-stay-after-call","expireTime":"2022-11-30T07:29:12.153+0000","updated":"2022-09-01T07:29:12.153+0000","redirectUri":"https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"}""",
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plus(89, ChronoUnit.DAYS))))
                .build();

        // when
        AccessMeansDTO newAccessMeans = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(_89DaysFromNow).isBefore(newAccessMeans.getExpireTime());
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans accessMeans = OpenBankingTestObjectMapper.INSTANCE.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("shinny-new-access-token");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("refresh-token-which-should-be-stay-after-call");
        assertThat(_89DaysFromNow).isBefore(accessMeans.getExpireTime());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldOnUserSiteDelete(UrlDataProvider dataProvider) {
        // given
        String externalConsentId = "363ca7c1-9d03-4876-8766-ddefc9fd2d76";

        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private ProviderAccountDTO findProviderAccountDTO(final List<ProviderAccountDTO> accountDTOs, final String accountId) {
        return accountDTOs.stream()
                .filter(providerAccountDTO -> providerAccountDTO.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AssertionError(String.format("ProviderAccountDTO with id %s was not found", accountId)));
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                bankOfScotlandDataProviderV10,
                bankOfScotlandCorpoDataProviderV8,
                halifaxDataProviderV10,
                lloydsBankDataProviderV10,
                lloydsBankCorpoDataProviderV8,
                mbnaCreditCardDataProviderV6
        );
    }

    private Stream<Arguments> getDataProvidersWithExpectedAudience() {
        return Stream.of(
                Arguments.of(bankOfScotlandDataProviderV10, "bos"),
                Arguments.of(bankOfScotlandCorpoDataProviderV8, "bos"),
                Arguments.of(halifaxDataProviderV10, "hfx"),
                Arguments.of(lloydsBankDataProviderV10, "lyds"),
                Arguments.of(lloydsBankCorpoDataProviderV8, "lyds"),
                Arguments.of(mbnaCreditCardDataProviderV6, "mbn")
        );
    }
}
