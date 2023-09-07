package com.yolt.providers.openbanking.ais.aibgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AibGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("aib")
@AutoConfigureWireMock(stubs = "classpath:/stubs/aibgroup/v31/client_secret/happy-flow/", httpsPort = 0, port = 0)
class AibGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final Instant TRANSACTIONS_FROM = Instant.parse("2015-01-01T00:00:00Z");
    private static final Signer SIGNER = new SignerMock();

    private static String SERIALIZED_ACCESS_MEANS;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> UUID.randomUUID().toString());

    @Autowired
    @Qualifier("AibDataProviderV6")
    private GenericBaseDataProvider aibDataProviderV6;

    @Autowired
    @Qualifier("AibNIDataProviderV6")
    private GenericBaseDataProvider aibNIDataProviderV6;

    @Autowired
    @Qualifier("AibIeDataProviderV1")
    private GenericBaseDataProvider aibIeDataProviderV1;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<GenericBaseDataProvider> getAibProviders() {
        return Stream.of(aibDataProviderV6, aibNIDataProviderV6, aibIeDataProviderV1);
    }

    private Stream<GenericBaseDataProvider> getAibStableProviders() {
        return Stream.of(aibDataProviderV6, aibNIDataProviderV6, aibIeDataProviderV1);
    }

    private Stream<GenericBaseDataProvider> getAibProvidersUsingEidas() {
        return Stream.of(aibIeDataProviderV1);
    }

    private Stream<GenericBaseDataProvider> getAibProvidersUsingObCerts() {
        return Stream.of(aibDataProviderV6, aibNIDataProviderV6);
    }

    @BeforeAll
    void setup() throws JsonProcessingException {
        AccessMeans token = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        SERIALIZED_ACCESS_MEANS = objectMapper.writeValueAsString(token);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = AibGroupSampleAuthenticationMeans.getAibGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getAibProvidersUsingObCerts")
    void shouldReturnCorrectRedirectStepForGetLoginInfoWithCorrectRequestData(GenericBaseDataProvider aibDataProvider) {
        // given
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) aibDataProvider.getLoginInfo(urlGetLogin);

        // then
        String expectedUrlRegex = ".*\\/authorize\\?response_type=code\\+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=openid\\+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt\\.com%2Fidentifier&request=.*";
        assertThat(loginInfo.getRedirectUrl()).matches(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("363ca7c1-9d03-4876-8766-ddefc9fd2d76");
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldReturnCorrectResponseForFetchDataWithCorrectRequestData(GenericBaseDataProvider aibDataProvider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(TRANSACTIONS_FROM)
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = aibDataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);

        Optional<ProviderAccountDTO> optionalAccount0 = dataProviderResponse.getAccounts().stream().filter(account -> "123".equals(account.getAccountId())).findFirst();
        assertThat(optionalAccount0).isPresent();
        ProviderAccountDTO account0 = optionalAccount0.get();
        assertThat(account0.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account0.getAvailableBalance()).isEqualTo("769.96");
        assertThat(account0.getCurrentBalance()).isEqualTo("804.57");
        assertThat(account0.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account0.getAccountNumber().getIdentification()).isEqualTo("GB15AIBK12345678901234");
        assertThat(account0.getName()).isEqualTo("STUDENT-062");
        assertThat(account0.getBic()).isEqualTo("FTBKGB2B");
        assertThat(account0.getCurrency()).isEqualTo(CurrencyCode.GBP);
        //directDebits
        assertThat(account0.getDirectDebits()).hasSize(1);
        DirectDebitDTO directDebit0 = account0.getDirectDebits().get(0);
        assertThat(directDebit0.getDirectDebitId()).isEqualTo("16421382");
        assertThat(directDebit0.getDescription()).isEqualTo("1STCENTRALFINANCE");
        assertThat(directDebit0.getPreviousPaymentAmount()).isEqualTo("61.54");
        assertThat(directDebit0.getPreviousPaymentDateTime()).isEqualTo("2020-02-18T00:00Z");
        assertThat(directDebit0.isDirectDebitStatus()).isTrue();
        //standingOrders
        assertThat(account0.getStandingOrders()).hasSize(1);
        StandingOrderDTO standingOrder0 = account0.getStandingOrders().get(0);
        assertThat(standingOrder0.getStandingOrderId()).isEqualTo("000001");
        assertThat(standingOrder0.getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(standingOrder0.getDescription()).isEqualTo("119 UV F3 C.HEANEY");
        assertThat(standingOrder0.getNextPaymentDateTime()).isEqualTo("2016-04-01T00:00Z");
        assertThat(standingOrder0.getNextPaymentAmount()).isEqualTo("0.00");
        assertThat(standingOrder0.getFinalPaymentDateTime()).isEqualTo("2016-07-01T00:00Z");
        assertThat(standingOrder0.getCounterParty().getHolderName()).isEqualTo("DOUGLAS DOUGLAS");
        assertThat(standingOrder0.getCounterParty().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(standingOrder0.getCounterParty().getIdentification()).isEqualTo("95061120076953");
        //transactions
        assertThat(account0.getTransactions()).hasSize(5);
        Optional<ProviderTransactionDTO> optionalTransaction0 = account0.getTransactions()
                .stream()
                .filter(tr -> "VDP-AMAZON. AMAZON".equals(tr.getDescription()))
                .findFirst();
        assertThat(optionalTransaction0).isPresent();
        ProviderTransactionDTO transaction0 = optionalTransaction0.get();
        assertThat(transaction0.getDateTime()).isEqualTo("2021-02-04T00:00Z[Europe/London]");
        assertThat(transaction0.getAmount()).isEqualTo("9.01");
        assertThat(transaction0.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction0.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        Optional<ProviderTransactionDTO> optionalTransaction1 = account0.getTransactions()
                .stream()
                .filter(tr -> "AMZN DIGITAL 35312".equals(tr.getDescription()))
                .findFirst();
        assertThat(optionalTransaction1).isPresent();
        ProviderTransactionDTO transaction1 = optionalTransaction1.get();
        assertThat(transaction1.getDateTime()).isEqualTo("2021-02-08T00:00Z[Europe/London]");
        assertThat(transaction1.getAmount()).isEqualTo("11.62");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.CREDIT);

        Optional<ProviderAccountDTO> optionalAccount1 = dataProviderResponse.getAccounts()
                .stream()
                .filter(account -> "1234567".equals(account.getAccountId()))
                .findFirst();
        assertThat(optionalAccount1).isPresent();

        assertThat(optionalAccount1.get().getName()).isEqualTo(aibDataProvider.getProviderIdentifierDisplayName() + " Account");
    }

    @ParameterizedTest
    @MethodSource("getAibStableProviders")
    void shouldReturnCorrectResponseForRefreshAccessMeansWithCorrectRequestData(GenericBaseDataProvider aibDataProvider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = aibDataProvider.refreshAccessMeans(urlRefreshAccessMeans);

        //then
        AccessMeans deserializedOAuthToken = objectMapper.readValue(retrievedAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("0a85daa7-a544-4472-9d16-91715fc5641f");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("23a88b8a-0580-4b50-a0ad-32fb97dcdbae");
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldReturnCorrectResponseForCreatAccessMeansWithCorrectRequestData(GenericBaseDataProvider aibDataProvider) throws JsonProcessingException {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545#code=" + authorizationCode + "&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = aibDataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);
        AccessMeans accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("0a85daa7-a544-4472-9d16-91715fc5641f");
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldPerformOnUserSiteDeleteWithCorrectRequestWithNoError(GenericBaseDataProvider aibDataProvider) throws TokenInvalidException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setExternalConsentId("363ca7c1-9d03-4876-8766-ddefc9fd2d76")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        //when
        Throwable call = catchThrowable(() -> aibDataProvider.onUserSiteDelete(urlGetLogin));

        //then
        assertThat(call).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getAibProvidersUsingObCerts")
    void shouldReturnTypedAuthenticationMeansWithSsaAndSoftwareId(GenericBaseDataProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = provider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).containsOnlyKeys(
                AibGroupAuthMeansBuilderV3.INSTITUTION_ID_NAME,
                AibGroupAuthMeansBuilderV3.CLIENT_ID_NAME,
                AibGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME,
                AibGroupAuthMeansBuilderV3.SIGNING_KEY_HEADER_ID_NAME,
                AibGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME,
                AibGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME,
                AibGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME,
                AibGroupAuthMeansBuilderV3.SOFTWARE_ID_NAME,
                AibGroupAuthMeansBuilderV3.SOFTWARE_STATEMENT_ASSERTION_NAME,
                AibGroupAuthMeansBuilderV3.ORGANIZATION_ID_NAME);
    }

    @ParameterizedTest
    @MethodSource("getAibProvidersUsingEidas")
    void shouldReturnTypedAuthenticationMeansWithoutSsaAndSoftwareId(GenericBaseDataProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = provider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).containsOnlyKeys(
                AibGroupAuthMeansBuilderV3.INSTITUTION_ID_NAME,
                AibGroupAuthMeansBuilderV3.CLIENT_ID_NAME,
                AibGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME,
                AibGroupAuthMeansBuilderV3.SIGNING_KEY_HEADER_ID_NAME,
                AibGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME,
                AibGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME,
                AibGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME);
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldReturnTransportKeyRequirements(GenericBaseDataProvider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getAibProviders")
    void shouldReturnSigningKeyRequirements(GenericBaseDataProvider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }
}
