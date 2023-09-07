package com.yolt.providers.monorepogroup.chebancagroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.securityutils.signing.SignatureAlgorithm;
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
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CheBancaGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/chebancagroup.ais.happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("chebancagroup")
class CheBancaGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String STATE = "0fa8b824-fb95-11ec-b939-0242ac120002";
    private static final String KEY_ID_VALUE = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final String CLIENT_ID = "client123";
    private static final String CLIENT_SECRET = "clientSecret123";
    private static final String CLIENT_APP = "YOLT_APP";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Rome");

    @Autowired
    @Qualifier("CheBancaGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("CheBancaRestTemplateManager")
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("CheBancaDataProviderV1")
    private UrlDataProvider cheBancaGroupDataProvider;

    @Autowired
    private Clock clock;

    @Mock
    private SignerMock signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(cheBancaGroupDataProvider);
    }

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), KEY_ID_VALUE));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(SIGNING_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), KEY_ID_VALUE));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_SECRET));
        authenticationMeans.put(CLIENT_APP_ID, new BasicAuthenticationMean(ALIAS_STRING.getType(), CLIENT_APP));
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnTypedAuthenticationMeans(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).containsAllEntriesOf(Map.of(
                CLIENT_ID_NAME, CLIENT_ID_STRING,
                CLIENT_SECRET_NAME, CLIENT_SECRET_STRING,
                TRANSPORT_CERTIFICATE_ID_NAME, KEY_ID,
                TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM,
                SIGNING_CERTIFICATE_ID_NAME, KEY_ID,
                SIGNING_CERTIFICATE_NAME, CERTIFICATE_PEM,
                CLIENT_APP_ID, ALIAS_STRING
        ));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStep(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setSigner(signer)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();

        // when
        var redirectStep = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        UriComponents loginUrl = UriComponentsBuilder.fromHttpUrl(redirectStep.getRedirectUrl()).build();
        assertThat(loginUrl).satisfies(url -> {
                    assertThat(url.getScheme()).isEqualTo("https");
                    assertThat(url.getHost()).isEqualTo("clienti.chebanca.it");
                    assertThat(url.getPath()).isEqualTo("/auth/oauth/v2/authorize/login");
                    assertThat(url.getQueryParams().toSingleValueMap()).containsAllEntriesOf(
                            Map.of("action", "display",
                                    "sessionID", "session123",
                                    "sessionData", "sessionData123")
                    );
                }
        );

        assertThat(redirectStep.getExternalConsentId()).isBlank();
        assertThat(redirectStep.getProviderState()).isBlank();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnNewAccessMeans(UrlDataProvider dataProvider) {
        // given
        var request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setRedirectUrlPostedBackFromSite("https://yolt-redirect.com?code=code123&state=a7f96425-304d-4e97-9665-e330fb8204d2")
                .setUserId(USER_ID)
                .setSigner(signer)
                .build();

        // when
        var result = dataProvider.createNewAccessMeans(request);

        //then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(USER_ID);


        assertThat(result.getAccessMeans().getUpdated()).isEqualTo(Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()));
        assertThat(result.getAccessMeans().getExpireTime()).isEqualTo(Date.from(LocalDate.of(2022, 1, 1).atStartOfDay(ZONE_ID).plusSeconds(600).toInstant()));
        assertThat(result.getAccessMeans().getAccessMeans()).contains(
                "scope", "ais",
                "refreshTokenValidityTimeInSeconds", "1200",
                "accessToken", "accessToken123",
                "refreshToken", "refreshToken123",
                "tokenType", "Bearer",
                "tokenValidityTimeInSeconds", "600"
        );
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRefreshedAccessMeans(UrlDataProvider dataProvider) throws TokenInvalidException {
        // given
        String accessMeans = "{\"scope\":\"ais\",\"refreshTokenValidityTimeInSeconds\":\"1200\",\"accessToken\":\"accessToken123\",\"refreshToken\":\"refreshToken123\",\"tokenType\":\"Bearer\",\"tokenValidityTimeInSeconds\":600}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                accessMeans,
                Date.from(Instant.now(clock)),
                Date.from(Instant.now(clock)));

        var request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .build();

        // when
        var result = dataProvider.refreshAccessMeans(request);

        //then
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getUpdated()).isEqualTo(Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()));
        assertThat(result.getExpireTime()).isEqualTo(Date.from(LocalDate.of(2022, 1, 1).atStartOfDay(ZONE_ID).plusSeconds(600).toInstant()));
        assertThat(result.getAccessMeans()).contains(
                "scope",
                "refreshTokenValidityTimeInSeconds", "1200",
                "accessToken", "newAccessToken123",
                "refreshToken", "newRefreshToken123",
                "tokenType", "Bearer",
                "tokenValidityTimeInSeconds", "600"
        );
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        String accessMeans = "{\"scope\":\"ais\",\"refreshTokenValidityTimeInSeconds\":\"1200\",\"accessToken\":\"accessToken123\",\"refreshToken\":\"refreshToken123\",\"tokenType\":\"Bearer\",\"tokenValidityTimeInSeconds\":600}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                accessMeans,
                Date.from(Instant.now(clock)),
                Date.from(Instant.now(clock)));

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setTransactionsFetchStartTime(Instant.now(clock))
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
                .availableBalance(new BigDecimal("200.00"))
                .currentBalance(new BigDecimal("100.00"))
                .accountId("account123")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "IT60X0542811101000000123123"))
                .name("Product nickname123")
                .currency(CurrencyCode.EUR)
                .transactions(List.of(createTransaction1ForAccount1(), createTransaction2ForAccount1()
                ,createTransaction3ForAccount1(),createTransaction4ForAccount1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("account123")
                        .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("IT60X0542811101000000123123")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Product nickname123")
                        .product("AZIM")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                       .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("100.00"))
                                                .build())
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .referenceDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("200.00"))
                                                .build())
                                        .balanceType(BalanceType.FORWARD_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                                        .build()))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction1ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("456")
                .dateTime(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("222.22"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Some kind of short description -222.22")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-222.22"))
                                .build())
                        .remittanceInformationUnstructured("Some kind of short description -222.22")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction2ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("654")
                .dateTime(LocalDate.of(2022, 07, 22).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("44.00"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Some kind of short description -44.00")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(LocalDate.of(2022, 07, 22).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 22).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-44.00"))
                                .build())
                        .remittanceInformationUnstructured("Some kind of short description -44.00")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction3ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("123")
                .dateTime(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("111.11"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.CREDIT)
                .description("Some kind of short description 111.11")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.PENDING)
                        .bookingDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("111.11"))
                                .build())
                        .remittanceInformationUnstructured("Some kind of short description 111.11")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction4ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("321")
                .dateTime(LocalDate.of(2022, 07, 22).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("33.00"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.CREDIT)
                .description("Some kind of short description 33.00")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.PENDING)
                        .bookingDate(LocalDate.of(2022, 07, 22).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 22).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("33.00"))
                                .build())
                        .remittanceInformationUnstructured("Some kind of short description 33.00")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderAccountDTO createAccount2() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CREDIT_CARD)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("300.00"))
                .currentBalance(new BigDecimal("200.00"))
                .accountId("account456")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "IT60X0542811101000000123456"))
                .name("Product nickname456")
                .currency(CurrencyCode.EUR)
                .transactions(List.of(createTransaction1ForAccount2(), createTransaction2ForAccount2()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("account456")
                        .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("IT60X0542811101000000123456")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Product nickname456")
                        .product("CAPC")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                       .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("200.00"))
                                                .build())
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .referenceDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("300.00"))
                                                .build())
                                        .balanceType(BalanceType.FORWARD_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                                        .build()))
                        .build())

                .build();
    }

    private ProviderTransactionDTO createTransaction1ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("789")
                .dateTime(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("555.55"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Some kind of short description -555.55")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-555.55"))
                                .build())
                        .remittanceInformationUnstructured("Some kind of short description -555.55")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction2ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("678")
                .dateTime(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("444.44"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.CREDIT)
                .description("Some kind of short description 444.44")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.PENDING)
                        .bookingDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 26).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("444.44"))
                                .build())
                        .remittanceInformationUnstructured("Some kind of short description 444.44")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }
}