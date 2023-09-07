package com.yolt.providers.abancagroup.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abancagroup.TestApp;
import com.yolt.providers.abancagroup.abanca.config.AbancaProperties;
import com.yolt.providers.abancagroup.common.AbancaSampleAuthenticationMeans;
import com.yolt.providers.abancagroup.common.TestRestTemplateManager;
import com.yolt.providers.abancagroup.common.TestSigner;
import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaTokens;
import com.yolt.providers.abancagroup.common.ais.auth.dto.TestAbancaAuthData;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.abancagroup.common.AbancaSampleAuthenticationMeans.CLIENT_ID_SAMPLE;
import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow", httpsPort = 0, port = 0)
class AbancaDataProviderV1IntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "123.45.67.89";
    private static final String CONSENT_ID = "7216950142828544";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final long FIXED_TIMESTAMP = 1000000000L;

    private TestRestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Signer signer;

    @Autowired
    @Qualifier("AbancaDataProviderV1")
    private AbancaDataProviderV1 AbancaRoDataProvider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private AbancaProperties properties;

    @MockBean
    private Clock clock;

    @Autowired
    @Qualifier("AbancaGroupObjectMapper")
    private ObjectMapper objectMapper;

    public Stream<AbancaDataProviderV1> getDataProviders() {
        return Stream.of(AbancaRoDataProvider);
    }

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(FIXED_TIMESTAMP));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = new AbancaSampleAuthenticationMeans().getAuthenticationMeans();
        signer = new TestSigner();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepOnGetLoginInfo(AbancaDataProviderV1 dataProvider) {
        // given
        String redirectUrl = "https://yolt.com/callback-acc";

        String state = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(redirectUrl)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        RedirectStep step = dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(step.getRedirectUrl()).contains(properties.getBaseUrl() + "/oauth/" + CLIENT_ID_SAMPLE + "/Abanca?")
                .contains("scope=Accounts Transactions")
                .contains("redirect_uri=" + redirectUrl)
                .contains("state=" + state)
                .contains("response_type=code");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeans(AbancaDataProviderV1 dataProvider) throws JsonProcessingException {
        // given
        String redirectUrl = "https://yolt.com/callback-acc?code=test-code";
        String baseUrl = "https://yolt.com/callback-acc";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setBaseClientRedirectUrl(baseUrl)
                .setProviderState(CONSENT_ID)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new AbancaTokens(
                new TestAbancaAuthData("new_access_token",
                        1199L,
                        "new_refresh_token"),
                clock)
        );

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans().getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshTokenSuccessfully(AbancaDataProviderV1 dataProvider) throws TokenInvalidException, JsonProcessingException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new AbancaTokens(
                new TestAbancaAuthData("new_refreshed_access_token",
                        1199L,
                        "test-refresh-token"),
                clock)
        );
        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldFetchDataSuccessfully(AbancaDataProviderV1 subject) throws TokenInvalidException, JsonProcessingException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setUserId(TEST_USER_ID)
                .setSigner(signer)
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(account1(), account2())
        );
    }

    private ZonedDateTime toZonedTime(int year, int month, int day, int hour, int minute, int seconds, int nanoOfSecond) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, seconds, nanoOfSecond), ZoneId.of(("Europe/Madrid")));
    }

    private ProviderAccountDTO account1() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "ES1920800531183000027485");
        accountNumber_account1.setHolderName("INMACULADA IGLESIAS FERNANDEZ");
        return ProviderAccountDTO.builder()
                .accountId("10486ABC01F20362F29063237FE31EFF352E759C08B608B08C944CB6E9F1F6D4")
                .accountNumber(accountNumber_account1)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(BigDecimal.valueOf(285570.24))
                .currentBalance(BigDecimal.valueOf(285570.24))
                .currency(CurrencyCode.EUR)
                .name("10486ABC01F20362F29063237FE31EFF352E759C08B608B08C944CB6E9F1F6D4")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(List.of(
                        account1Transaction1(),
                        account1Transaction2(),
                        account1Transaction3()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("10486ABC01F20362F29063237FE31EFF352E759C08B608B08C944CB6E9F1F6D4")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("ES1920800531183000027485")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("10486ABC01F20362F29063237FE31EFF352E759C08B608B08C944CB6E9F1F6D4")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(285570.24))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("AEAC0A7A8486BB0F4CC262B454AD6A91F5445C430E6E72722070754016E38B03")
                .dateTime(toZonedTime(2021, 6, 17, 10, 06, 15, 440869300))
                .amount(BigDecimal.valueOf(197.81))
                .status(BOOKED)
                .type(CREDIT)
                .description("PAGO COMUNIDAD, TRF.PERIODICA: 1")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("AEAC0A7A8486BB0F4CC262B454AD6A91F5445C430E6E72722070754016E38B03")
                        .bookingDate(toZonedTime(2021, 6, 17, 10, 06, 15, 440869300))
                        .valueDate(toZonedTime(2021, 6, 17, 10, 06, 15, 440869300))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(197.81))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("5694D524D079774CC2FCB02BDA620721046358E78ED0EBADC976C0F6C26B79AE")
                .dateTime(toZonedTime(2021, 8, 13, 11, 18, 19, 5075800))
                .amount(BigDecimal.valueOf(375.93))
                .status(BOOKED)
                .type(CREDIT)
                .description("DISTRIBUCIONES FROIZ, S.A.U.,FRA 01/901/22-2102 DE 0")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("5694D524D079774CC2FCB02BDA620721046358E78ED0EBADC976C0F6C26B79AE")
                        .bookingDate(toZonedTime(2021, 8, 13, 11, 18, 19, 5075800))
                        .valueDate(toZonedTime(2021, 8, 13, 11, 18, 19, 5075800))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(375.93))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction3() {
        return ProviderTransactionDTO.builder()
                .externalId("21CA17E6D6FCAD8D873E9A7C3D96781D86DB3BFCF71B92D3141FA1751B838960")
                .dateTime(toZonedTime(2021, 8, 13, 11, 22, 9, 388404800))
                .amount(BigDecimal.valueOf(6177.61))
                .status(BOOKED)
                .type(CREDIT)
                .description("FICH. TRFS: B364073690002021-02-05T13:33")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("21CA17E6D6FCAD8D873E9A7C3D96781D86DB3BFCF71B92D3141FA1751B838960")
                        .bookingDate(toZonedTime(2021, 8, 13, 11, 22, 9, 388404800))
                        .valueDate(toZonedTime(2021, 8, 13, 11, 22, 9, 388404800))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(6177.61))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderAccountDTO account2() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "ES5420805148513040004380");
        accountNumber_account1.setHolderName(" TRANSHORMIGALIA, S.L. ");
        return ProviderAccountDTO.builder()
                .accountId("D208D3D2D4D3014B9DE3770A222E11BAF2CFD59E44DDA9400A654D5586A29018")
                .accountNumber(accountNumber_account1)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(BigDecimal.valueOf(285570.24))
                .currentBalance(BigDecimal.valueOf(285570.24))
                .currency(CurrencyCode.EUR)
                .name("D208D3D2D4D3014B9DE3770A222E11BAF2CFD59E44DDA9400A654D5586A29018")
                .yoltAccountType(AccountType.CREDIT_CARD)
                .creditCardData(new ProviderCreditCardDTO())
                .transactions(List.of(account2Transaction1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("D208D3D2D4D3014B9DE3770A222E11BAF2CFD59E44DDA9400A654D5586A29018")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("ES5420805148513040004380")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("D208D3D2D4D3014B9DE3770A222E11BAF2CFD59E44DDA9400A654D5586A29018")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(285570.24))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .build()))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account2Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("784F36B04966704B6026140CD3E7ACCDFD90E17673EBE2E5CDC8F1B29F6ABAE5")
                .dateTime(toZonedTime(2021, 8, 13, 11, 22, 9, 388404800))
                .amount(BigDecimal.valueOf(248.99))
                .status(BOOKED)
                .type(CREDIT)
                .description("PAGARE 000361681")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("784F36B04966704B6026140CD3E7ACCDFD90E17673EBE2E5CDC8F1B29F6ABAE5")
                        .bookingDate(toZonedTime(2021, 8, 13, 11, 22, 9, 388404800))
                        .valueDate(toZonedTime(2021, 8, 13, 11, 22, 9, 388404800))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(248.99))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .build())
                .build();
    }

    private AccessMeansDTO getAccessMeans() throws JsonProcessingException {
        AbancaTokens tokens = new AbancaTokens(new TestAbancaAuthData(
                "test-access-token",
                3600L,
                "test-refresh-token"), clock);

        return new AccessMeansDTO(TEST_USER_ID,
                objectMapper.writeValueAsString(tokens),
                new Date(clock.millis()),
                new Date(clock.millis()));
    }

}