package com.yolt.providers.openbanking.ais.santander.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.santander.SantanderApp;
import com.yolt.providers.openbanking.ais.santander.SantanderSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.santander.dto.SantanderAccessMeansV2;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains all happy flows occurring in Santander group group providers.
 * Covered flows:
 * - updating authentication means using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders, direct debits
 * - flow do not break when standing orders endpoint returns HTTP 500
 * - flow do not break when direct debits endpoint returns HTTP 500
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SantanderApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("santander")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/santander/ais-3.1.6/happy-flow"}, httpsPort = 0, port = 0)
public class SantanderDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Signer SIGNER = new SignerMock();
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");

    private static AccessMeansState<SantanderAccessMeansV2> token;

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("SantanderDataProviderV17")
    private GenericBaseDataProviderV2 santanderDataProviderV17;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper OBJECT_MAPPER;

    @BeforeAll
    public static void setup() {
        token = new AccessMeansState<>(new SantanderAccessMeansV2(
                Instant.now(),
                USER_ID,
                "AAIkMDM5NDJmZTUtOGNiMi00NzVmLWIwMTItNDgyZjM0ZTExYzI58q0t070fBgubnd8pgwu3kCwNt91ZJhhW3wfUl2UulSRjiKcfWfQQ9J9i8OU2QOSciVIl8mQ69GO7mDZ0uEv8INrboRu4fesBmEMq7PS87O7LrN7isyqwzpjKXBZR2JJkL3nF10SuDt_l4SItojPO4",
                "qx3scq02pKLSkSJklsjDJwi8SJN82kSD44tGLSLKjsiojw89mDMUIHMDSIUyw89m2DuTlkCwRFxY0xSsKQuYAC6BinbvjksHMFIsihmsiuHMISUIW88w78SMJI8smjKMSJHKJSHMWIWSHIUGWUIgukwgjhskjshhkjsjkdhmsjkhdgshjhgsfsdfwefefwsefsegsdgsdfasjhguiynGUYFGU",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                "redirect"),
                List.of("ReadParty")
        );
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "1626df30-50ad-42d8-8f39-40dd95f4b15f";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = new SantanderSampleAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnConsentPageUrl(UrlDataProvider provider) {
        // given
        String clientId = "422a2265-a061-4007-99b3-ceeb64e85077";
        String loginState = UUID.randomUUID().toString();
        String expectedUrlRegex = ".*\\/authorize\\?response_type=code\\+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=openid\\+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt\\.com%2Fcallback&request=.*";
        UrlGetLoginRequest urlLoginContext = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/callback").setState(loginState)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) provider.getLoginInfo(urlLoginContext);

        // then
        assertThat(loginInfo.getRedirectUrl()).matches(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("363ca7c1-9d03-4876-8766-ddefc9fd2d76");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnCorrectFetchData(UrlDataProvider provider) throws Exception {
        // given
        Instant fromFetchDate = Instant.parse("2015-01-01T00:00:00Z");
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(fromFetchDate)
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        decimalFormat.setParseBigDecimal(true);
        BigDecimal amount = (BigDecimal) decimalFormat.parse("1,000.00");

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(5);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);

        assertThat(providerAccountDTO.getExtendedAccount()).isNotNull();
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(amount);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("-1.00"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO.getName()).isEqualTo("Santander account");
        assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo("MR User Name Surname");
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(extendedAccount.getName()).isEqualTo("Santander account");
        assertThat(extendedAccount.getBalances()).hasSameElementsAs(getExtendedBalancesForCurrentAccount());

        // Verify Standing Order
        assertThat(providerAccountDTO.getStandingOrders()).hasSize(1);
        assertThat(providerAccountDTO.getStandingOrders().get(0).getDescription()).isEqualTo("Towbar Club 2 - We Love Towbars");
        assertThat(providerAccountDTO.getStandingOrders().get(0).getFrequency()).isEqualTo(Period.ofDays(1));
        assertThat(providerAccountDTO.getStandingOrders().get(0).getNextPaymentAmount()).isEqualTo(new BigDecimal("0.56"));
        assertThat(providerAccountDTO.getStandingOrders().get(0).getCounterParty().getIdentification()).isEqualTo("80200112345678");

        // Verify Direct Debit
        assertThat(providerAccountDTO.getDirectDebits()).hasSize(1);
        assertThat(providerAccountDTO.getDirectDebits().get(0).getDescription()).isEqualTo("Towbar Club 3 - We Love Towbars");
        assertThat(providerAccountDTO.getDirectDebits().get(0).isDirectDebitStatus()).isTrue();
        assertThat(providerAccountDTO.getDirectDebits().get(0).getPreviousPaymentAmount()).isEqualTo(new BigDecimal("0.57"));

        // Verify balances for Credit Cards
        ProviderAccountDTO providerAccountDTOCreditCard = dataProviderResponse.getAccounts().get(2);
        assertThat(providerAccountDTOCreditCard.getAvailableBalance()).isEqualTo(new BigDecimal("0.02"));
        assertThat(providerAccountDTOCreditCard.getCurrentBalance()).isEqualTo(new BigDecimal("0.01"));
        assertThat(providerAccountDTOCreditCard.getExtendedAccount().getBalances()).hasSameElementsAs(getExtendedBalancesForCreditCard());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnFetchAccountDataWithOptionalEndpoints500(UrlDataProvider provider) throws Exception {
        // given
        Instant fromFetchDate = Instant.parse("2015-01-01T00:00:00Z");
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(fromFetchDate)
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        //then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts.size()).isEqualTo(5);
        assertThat(accounts.stream()
                .filter(account -> account.getStandingOrders().isEmpty() &&
                        account.getDirectDebits().isEmpty() &&
                        account.getYoltAccountType() != AccountType.CREDIT_CARD
                )
                .count())
                .isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithAccessMeansStateType")
    void shouldCorrectRefreshAccessMeans(UrlDataProvider provider, TypeReference type) throws Exception {
        // given
        requestTraceId = "1626df30-50ad-42d8-8f39-40dd95f4b15f ";
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(), new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = provider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        SantanderAccessMeansV2 deserializedOAuthToken = extractAccessMeansFromSerializedJson(retrievedAccessMeans.getAccessMeans(), type);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("AAIkMDM5NDJmZTUtOGNiMi00NzVmLWIwMTItNDgyZjM0ZTExYzI58q0t070fBgubnd8pgwu3kCwNt91ZJhhW3wfUl2UulSRjiKcfWfQQ9J9i8OU2QOSciVIl8mQ69GO7mDZ0uEv8INrboRu4fesBmEMq7PS87O7LrN7isyqwzpjKXBZR2JJkL3nF10SuDt_l4SItojPO4");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("qx3scq02pKLSkSJklsjDJwi8SJN82kSD44tGLSLKjsiojw89mDMUIHMDSIUyw89m2DuTlkCwRFxY0xSsKQuYAC6BinbvjksHMFIsihmsiuHMISUIW88w78SMJI8smjKMSJHKJSHMWIWSHIUGWUIgukwgjhskjshhkjsjkdhmsjkhdgshjhgsfsdfwefefwsefsegsdgsdfasjhguiynGUYFGU");
    }

    private SantanderAccessMeansV2 extractAccessMeansFromSerializedJson(String json, TypeReference type) throws JsonProcessingException {
        var accessMeansState = OBJECT_MAPPER.readValue(json, type);
        return accessMeansState instanceof AccessMeansState ?
                ((AccessMeansState<SantanderAccessMeansV2>) accessMeansState).getAccessMeans() :
                (SantanderAccessMeansV2) accessMeansState;
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithAccessMeansStateType")
    void shouldCreateNewAccessMeans(UrlDataProvider provider, TypeReference type) throws Exception {
        //To reset counters
        resetAllRequests();

        // given
        requestTraceId = "1626df30-50ad-42d8-8f39-40dd95f4b15f";
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545#code=" + authorizationCode + "&state=secretState";
        String encodedUrl = URLEncoder.encode("https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545", "UTF-8");

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
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
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO newAccessMeans = provider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans token = extractAccessMeansFromSerializedJson(newAccessMeans.getAccessMeans(), type);
        assertThat(token.getAccessToken()).isEqualTo("AAIkMDM5NDJmZTUtOGNiMi00NzVmLWIwMTItNDgyZjM0ZTExYzI58q0t070fBgubnd8pgwu3kCwNt91ZJhhW3wfUl2UulSRjiKcfWfQQ9J9i8OU2QOSciVIl8mQ69GO7mDZ0uEv8INrboRu4fesBmEMq7PS87O7LrN7isyqwzpjKXBZR2JJkL3nF10SuDt_l4SItojPO4");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeserializeTokenWithUnknownFields(UrlDataProvider provider) throws IOException {
        // given
        String expectedAccessToken = "at12345";
        String expectedRefreshToken = "rt2345";
        String expectedExpireTime = "2018-01-11T12:13:14.123Z";
        String input = String.format(
                "{\"unknownField\": null, \"unknownField2\": null, \"accessToken\": \"%s\", \"refreshToken\": \"%s\", \"expireTime\": \"%s\"}",
                expectedAccessToken,
                expectedRefreshToken,
                expectedExpireTime);

        // when
        SantanderAccessMeansV2 output = OBJECT_MAPPER.readValue(input, SantanderAccessMeansV2.class);

        // then
        assertThat(output.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(output.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(output.getExpireTime()).isEqualTo(Date.from(Instant.parse(expectedExpireTime)));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldOnUserSiteDelete(UrlDataProvider provider) throws TokenInvalidException {
        // given
        String externalConsentId = "363ca7c1-9d03-4876-8766-ddefc9fd2d76";
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when -> then
        provider.onUserSiteDelete(urlGetLogin);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowGetAccessTokenFailedExceptionWhenErrorInQueryParameters(UrlDataProvider provider) {
        // given
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545?error=invalid_grant";
        UUID userId = UUID.randomUUID();

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
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
                .setSigner(SIGNER)
                .build();

        // when -> then
        assertThatThrownBy(() -> provider.createNewAccessMeans(urlCreateAccessMeans))
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnTransportKeyRequirements(UrlDataProvider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnSigningKeyRequirements(UrlDataProvider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnTypedAuthenticationMeans(UrlDataProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans)
                .hasSize(6)
                .containsOnlyKeys(
                        INSTITUTION_ID_NAME,
                        CLIENT_ID_NAME,
                        PRIVATE_SIGNING_KEY_HEADER_ID_NAME,
                        SIGNING_PRIVATE_KEY_ID_NAME,
                        TRANSPORT_CERTIFICATE_NAME,
                        TRANSPORT_PRIVATE_KEY_ID_NAME
                );
    }

    private List<BalanceDTO> getExtendedBalancesForCurrentAccount() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_AVAILABLE)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("1000.00")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2017-12-15T12:16Z")).withZoneSameInstant(ZONE_ID))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2017-12-15T12:16Z")).withZoneSameInstant(ZONE_ID))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_BOOKED)
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2012-08-15T03:05:45Z")).withZoneSameInstant(ZONE_ID))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2012-08-15T03:05:45Z")).withZoneSameInstant(ZONE_ID))
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-1.00"))).build());
        return balanceList;
    }

    private List<BalanceDTO> getExtendedBalancesForCreditCard() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.OPENING_CLEARED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("0.01")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2017-12-15T12:19Z")).withZoneSameInstant(ZONE_ID))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2017-12-15T12:19Z")).withZoneSameInstant(ZONE_ID))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.FORWARD_AVAILABLE)
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2017-12-15T12:19Z")).withZoneSameInstant(ZONE_ID))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2017-12-15T12:19Z")).withZoneSameInstant(ZONE_ID))
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("0.02"))).build());
        return balanceList;
    }

    private String getSerializedAccessMeans() {
        try {
            return OBJECT_MAPPER.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken", e);
        }
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(santanderDataProviderV17);
    }

    private Stream<Arguments> getProvidersWithAccessMeansStateType() {
        return Stream.of(
                Arguments.of(santanderDataProviderV17, new TypeReference<AccessMeansState<SantanderAccessMeansV2>>() {
                })
        );
    }
}
