package com.yolt.providers.monorepogroup.qontogroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.monorepogroup.TestSigner;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = QontoGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/qontogroup/v2/ais/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("qontogroup")
class QontoGroupDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String STATE = "0fa8b824-fb95-11ec-b939-0242ac120002";
    private static final String KEY_ID_VALUE = "11111111-1111-1111-1111-111111111111";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final String CLIENT_ID = "THE-CLIENT-ID";
    private static final String CLIENT_SECRET = "THE-CLIENT-SECRET";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");


    @Autowired
    @Qualifier("QontoGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("QontoDataProviderV1")
    private UrlDataProvider qontoDataProvider;

    @Autowired
    private Clock clock;

    private Signer signer = new TestSigner();

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(qontoDataProvider);
    }

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(SIGNING_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), KEY_ID_VALUE));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));
    }


    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnTypedAuthenticationMeans(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).containsAllEntriesOf(Map.of(CLIENT_ID_NAME, CLIENT_ID_STRING, SIGNING_CERTIFICATE_ID_NAME, KEY_ID, SIGNING_CERTIFICATE_NAME, CLIENT_SIGNING_CERTIFICATE_PEM));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStep(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();
        var expectedRedirectUri = UriComponentsBuilder.fromUriString("https://myawesomeauthorizationurl.com/oauth2/authorize")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("response_type", "code")
                .queryParam("state", STATE)
                .queryParam("scope", "organization.read offline_access")
                .toUriString();

        // when
        var result = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(result.getRedirectUrl()).isEqualTo(expectedRedirectUri);
        assertThat(result.getExternalConsentId()).isNull();
        assertThat(result.getProviderState()).isNull();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnNewAccessMeans(UrlDataProvider dataProvider) {
        // given
        var request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?code=AWASOME_CODE&state=fa8b824-fb95-11ec-b939-0242ac120002")
                .setUserId(USER_ID)
                .build();
        var expectedAccessMeansOrState = new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        USER_ID,
                        "{\"accessToken\":\"AWESOME-ACCESS-TOKEN\",\"refreshToken\":\"AWESOME-REFRESH-TOKEN\",\"expirationTimeInMillis\":1640998800000}",
                        Date.from(clock.instant()),
                        Date.from(Instant.now(clock).plusSeconds(3600))
                )
        );

        // when
        var result = dataProvider.createNewAccessMeans(request);

        //then
        assertThat(result).isEqualTo(expectedAccessMeansOrState);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeans(UrlDataProvider dataProvider) throws TokenInvalidException {
        //given
        var request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setAccessMeans(USER_ID,
                        "{\"accessToken\":\"AWESOME-ACCESS-TOKEN\",\"refreshToken\":\"AWESOME-REFRESH-TOKEN\",\"expirationTimeInMillis\":1640998800000}",
                        Date.from(clock.instant()),
                        Date.from(Instant.now(clock).plusSeconds(3600)))
                .build();
        var expectedRefreshedAccessMeans = new AccessMeansDTO(
                USER_ID,
                "{\"accessToken\":\"SHINNY-NEW-ACCESS-TOKEN\",\"refreshToken\":\"SHINNY-NEW-REFRESH-TOKEN\",\"expirationTimeInMillis\":1640998799000}",
                Date.from(clock.instant()),
                Date.from(Instant.now(clock).plusSeconds(3599))
        );

        //when
        var result = dataProvider.refreshAccessMeans(request);

        //then
        assertThat(result).isEqualTo(expectedRefreshedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        var accessMeans = new AccessMeansDTO(USER_ID,
                "{\"accessToken\":\"AWESOME-ACCESS-TOKEN\",\"refreshToken\":\"AWESOME-REFRESH-TOKEN\",\"expirationTimeInMillis\":1640998800000}",
                java.util.Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()),
                java.util.Date.from(LocalDate.now(clock).plusDays(89).atStartOfDay(ZONE_ID).toInstant()));

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeans)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setSigner(signer)
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();
        // when
        var response = dataProvider.fetchData(request);
        // then
        assertThat(response).isEqualTo(expectedResponse);
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(List.of(createAccount1(), createAccount2()));
    }

    private ProviderAccountDTO createAccount1() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("28"))
                .currentBalance(new BigDecimal("28"))
                .accountId("scep-1111-bank-account-3")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "FR7616958000013097751007940"))
                .bic("QNTOFRP1XXX")
                .name("TEST STAGING")
                .currency(CurrencyCode.EUR)
                .transactions(List.of(createTransaction1ForAccount1(), createTransaction2ForAccount1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("scep-1111-bank-account-3")
                        .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("FR7616958000013097751007940")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("TEST STAGING")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .bic("QNTOFRP1XXX")
                        .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("28"))
                                                .build())
                                        .balanceType(BalanceType.INTERIM_BOOKED)
                                        .referenceDate(ZonedDateTime.of(2022, 07, 13, 2, 0, 18, 278000000, ZONE_ID))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("28"))
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(ZonedDateTime.of(2022, 07, 13, 2, 0, 18, 278000000, ZONE_ID))
                                        .build()))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction1ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("scep-1111-3-transaction-2")
                .dateTime(ZonedDateTime.of(2022, 07, 13, 02, 00, 16, 564000000, ZONE_ID))
                .amount(new BigDecimal("10"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Internal transfer")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(ZonedDateTime.of(2022, 07, 13, 02, 00, 16, 564000000, ZONE_ID))
                        .valueDate(ZonedDateTime.of(2022, 07, 13, 02, 00, 16, 494000000, ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-10"))
                                .build())
                        .creditorName("SCEP")
                        .remittanceInformationUnstructured("Internal transfer")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction2ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("scep-1111-3-transaction-1")
                .dateTime(ZonedDateTime.of(2022, 07, 13, 02, 00, 14, 367000000, ZONE_ID))
                .amount(new BigDecimal("18"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.CREDIT)
                .description("Internal transfer")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.PENDING)
                        .bookingDate(ZonedDateTime.of(2022, 07, 13, 02, 00, 14, 367000000, ZONE_ID))
                        .valueDate(ZonedDateTime.of(2022, 07, 13, 02, 00, 14, 268000000, ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("18"))
                                .build())
                        .debtorName("SCEP")
                        .remittanceInformationUnstructured("Internal transfer")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderAccountDTO createAccount2() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("30788971.25"))
                .currentBalance(new BigDecimal("31000876.08"))
                .accountId("scep-1111-bank-account-1")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "FR7616958000015974423718064"))
                .bic("QNTOFRP1XXX")
                .name("Compte principal")
                .currency(CurrencyCode.EUR)
                .transactions(List.of(createTransaction1ForAccount2(), createTransaction2ForAccount2(), createTransaction3ForAccount2()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("scep-1111-bank-account-1")
                        .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("FR7616958000015974423718064")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Compte principal")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .bic("QNTOFRP1XXX")
                        .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("31000876.08"))
                                                .build())
                                        .balanceType(BalanceType.INTERIM_BOOKED)
                                        .referenceDate(ZonedDateTime.of(2022, 07, 26, 13, 07, 18, 682000000, ZONE_ID))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("30788971.25"))
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(ZonedDateTime.of(2022, 07, 26, 13, 07, 18, 682000000, ZONE_ID))
                                        .build()))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction1ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("scep-1111-1-transaction-92")
                .dateTime(ZonedDateTime.of(2022, 07, 18, 10, 45, 19, 352000000, ZONE_ID))
                .amount(new BigDecimal("8.63"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.DEBIT)
                .description("DALMA-Ckvqyahrm")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.PENDING)
                        .bookingDate(ZonedDateTime.of(2022, 07, 18, 10, 45, 19, 352000000, ZONE_ID))
                        .valueDate(ZonedDateTime.of(2022, 07, 18, 10, 45, 19, 42000000, ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-8.63"))
                                .build())
                        .creditorName("Jane Skywalker")
                        .remittanceInformationUnstructured("DALMA-Ckvqyahrm")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction2ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("scep-1111-1-transaction-84")
                .dateTime(ZonedDateTime.of(2022, 07, 15, 12, 53, 54, 885000000, ZONE_ID))
                .amount(new BigDecimal("31000000.44"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.CREDIT)
                .description("SUMUP PID68275 PAYOUT 210520")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(ZonedDateTime.of(2022, 07, 15, 12, 53, 54, 885000000, ZONE_ID))
                        .valueDate(ZonedDateTime.of(2022, 07, 15, 10, 53, 54, 0, ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("31000000.44"))
                                .build())
                        .debtorName("SumUp Payments Limited")
                        .remittanceInformationUnstructured("SUMUP PID68275 PAYOUT 210520")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction3ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("scep-1111-1-transaction-73")
                .dateTime(ZonedDateTime.of(2022, 07, 13, 2, 00, 14, 367000000, ZONE_ID))
                .amount(new BigDecimal("18"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Internal transfer")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(ZonedDateTime.of(2022, 07, 13, 02, 00, 14, 367000000, ZONE_ID))
                        .valueDate(ZonedDateTime.of(2022, 07, 13, 02, 00, 14, 268000000, ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-18"))
                                .build())
                        .creditorName("TEST STAGING")
                        .remittanceInformationUnstructured("Internal transfer")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }
}