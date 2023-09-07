package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupApp;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupSampleTypedAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.VanquisGroupBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.properties.VanquisGroupPropertiesV2;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Vanquis bank provider.
 * <p>
 * Disclaimer: Vanquis is a single bank, so there is no need to parametrize this test class.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions
 * - creating access means
 * - refreshing access means
 * - autoonboarding
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {VanquisGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/vanquisgroup/ais/ob_3.1.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("vanquisgroupV1")
public class VanquisDataProviderV3HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "890";
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final SignerMock SIGNER = new SignerMock();
    private static String SERIALIZED_ACCESS_MEANS;
    private static String SERIALIZED_ACCESS_MEANS_EXTENDED;
    private final ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;

    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    @Qualifier("VanquisDataProviderV3")
    private VanquisGroupBaseDataProviderV2 vanquisDataProvider;

    @Autowired
    private VanquisGroupPropertiesV2 properties;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        Instant now = Instant.now();
        AccessMeans token = new AccessMeans(
                now,
                USER_ID,
                "accessToken456",
                "refreshToken456",
                Date.from(now.plus(1, ChronoUnit.DAYS)),
                Date.from(now),
                TEST_REDIRECT_URL);
        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);

        SERIALIZED_ACCESS_MEANS_EXTENDED = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = new VanquisGroupSampleTypedAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnTransportKeyRequirements(VanquisGroupBaseDataProviderV2 dataProvider) {
        // when
        KeyRequirements transportKeyRequirements = dataProvider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnSigningKeyRequirementsGenericBaseDataProvider(VanquisGroupBaseDataProviderV2 dataProvider) {
        // when
        KeyRequirements signingKeyRequirements = dataProvider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnTypedAuthenticationMeans(VanquisGroupBaseDataProviderV2 dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = dataProvider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                SOFTWARE_ID_NAME,
                SOFTWARE_STATEMENT_ASSERTION_NAME
        );
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnConsentPageUrl(VanquisGroupBaseDataProviderV2 dataProvider) {
        // given
        String redirectUrl = "http://yolt.com/identifier";
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        String expectedUrlRegex = MessageFormat.format("?response_type=code+id_token&client_id={0}&state={1}&scope=openid+accounts+offline_access&nonce={1}&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=",
                clientId, loginState);
        String expectedRequestPayloadRegexp = MessageFormat.format("""
                        \\'{'"iss":"{0}",\
                        "aud":"{1}",\
                        "response_type":\
                        "code id_token",\
                        "client_id":"{0}",\
                        "redirect_uri":"{2}",\
                        "scope":"openid accounts offline_access",\
                        "state":"{3}",\
                        "nonce":"{3}",\
                        "max_age":86400,"claims":\\'{'"userinfo":\\'{'"openbanking_intent_id":\\'{'"value":"50ca5ed5-317c-451c-8438-3b3fb91466e1","essential":true'}}',\
                        "id_token":\\'{'"openbanking_intent_id":\\'{'"value":"50ca5ed5-317c-451c-8438-3b3fb91466e1","essential":true'}',\
                        "acr":\\'{'"essential":true,"values":\\["urn:openbanking:psd2:sca","urn:openbanking:psd2:ca"]'}}}',\
                        "sub":"{0}",\
                        "exp":\\d+,\
                        "iat":\\d+,\
                        "jti":".+"'}'""",
                clientId, properties.getAudience(), redirectUrl, loginState);

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(redirectUrl)
                .setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        String loginUrl = loginInfo.getRedirectUrl();
        assertThat(loginUrl).contains(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");

        String requestPayload = extractRequestPayload(loginUrl);
        assertThat(requestPayload).matches(expectedRequestPayloadRegexp);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnCorrectlyFetchData(VanquisGroupBaseDataProviderV2 dataProvider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        Optional<ProviderAccountDTO> firstAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("890"))
                .findFirst();
        assertThat(firstAccount.isPresent()).isTrue();
        ProviderAccountDTO providerAccountDTO = firstAccount.get();
        assertThat(providerAccountDTO.getName()).isEqualTo("Credit Card");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("191.25"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("-209.57"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
        assertThat(extendedAccountDTO.getBalances()).hasSameElementsAs(getExtendedBalancesForCreditCard());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnFetchDataWithSpecificChanges(VanquisGroupBaseDataProviderV2 dataProvider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS_EXTENDED, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        validateAccount_890(providerAccountDTO);
        validateTransactions(providerAccountDTO.getTransactions());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCorrectRefreshAccessMeans(VanquisGroupBaseDataProviderV2 dataProvider) throws Exception {
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
        String serializedToken = objectMapper.writeValueAsString(token);
        accessMeans.setAccessMeans(serializedToken);
        accessMeans.setUserId(USER_ID);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO resultAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        AccessMeans deserializedOAuthToken = OpenBankingTestObjectMapper.INSTANCE.readValue(resultAccessMeans.getAccessMeans(), AccessMeans.class);

        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("SOME_REFRESH_TOKEN");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeans(VanquisGroupBaseDataProviderV2 dataProvider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";

        String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?code=" + authorizationCode + "&state=secretState";

        Date _29MinutesFromNow = Date.from(Instant.now().plus(4, ChronoUnit.MINUTES));

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_29MinutesFromNow.before(newAccessMeans.getExpireTime())).isTrue();
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans token = objectMapper.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(token.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(_29MinutesFromNow.before(token.getExpireTime())).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCorrectAutoOnboarding(VanquisGroupBaseDataProviderV2 dataProvider) {
        //Given
        authenticationMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(TEST_REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        //When
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        //Then
        assertThat(configureMeans.get(CLIENT_ID_NAME)).isNotNull();
    }

    @SneakyThrows(JoseException.class)
    private String extractRequestPayload(String loginUrl) {
        String requestObject = UriComponentsBuilder.fromUriString(loginUrl)
                .build()
                .getQueryParams()
                .get("request")
                .get(0);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(requestObject);
        return new String(Base64.getDecoder().decode(jws.getEncodedPayload()));
    }

    private List<BalanceDTO> getExtendedBalancesForCreditCard() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.PREVIOUSLY_CLOSED_BOOKED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-223.72")))
                .lastChangeDateTime(ZonedDateTime.parse("2020-11-23T17:48:45Z"))
                .referenceDate(ZonedDateTime.parse("2020-11-23T17:48:45Z"))
                .build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.OPENING_CLEARED)
                .lastChangeDateTime(ZonedDateTime.parse("2020-11-23T17:48:45Z"))
                .referenceDate(ZonedDateTime.parse("2020-11-23T17:48:45Z"))
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-209.57"))).build());
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.FORWARD_AVAILABLE)
                .lastChangeDateTime(ZonedDateTime.parse("2020-11-23T17:48:45Z"))
                .referenceDate(ZonedDateTime.parse("2020-11-23T17:48:45Z"))
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("191.25"))).build());
        return balanceList;
    }

    private void validateAccount_890(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("191.25"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("-209.57"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB11BARC20038015831118");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getName()).isEqualTo("Credit Card");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.MASKED_PAN).value("************7185").build()
        );
    }

    private void validateTransactions(List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getAmount()).isEqualTo(new BigDecimal("65.03"));
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);

        transaction1.validate();

        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getAmount()).isEqualTo(new BigDecimal("65.86"));
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);

        transaction2.validate();

        ProviderTransactionDTO transaction3 = transactions.get(2);
        assertThat(transaction3.getAmount()).isEqualTo(new BigDecimal("17.00"));
        assertThat(transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction3.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction3.getCategory()).isEqualTo(YoltCategory.GENERAL);

        transaction3.validate();
    }

    private Stream<Provider> getDataProviders() {
        return Stream.of(vanquisDataProvider);
    }
}
