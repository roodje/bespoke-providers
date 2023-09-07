package com.yolt.providers.abnamrogroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.AbnAmroDataProvider;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.data.BankConsentType;
import com.yolt.providers.abnamrogroup.common.data.TransactionTimestampDateExtractor;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.HashUtils;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
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
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/wiremock/", httpsPort = 0, port = 0)
@ActiveProfiles("test")
public class AbnAmroDataProviderIntegrationTest {

    private static final String REDIRECT_URL = "https://localhost/auth";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "TPP_test";
    private static final String API_KEY = "7zacIF8Cu5o3XF4gUll4sRGuI2gDYiCA";
    private static final UUID TRANSPORT_KEY_ID = UUID.randomUUID();
    private static final String CODE = "bIFj1_XrdWgW6kRvyVIXPqyRlaPA2DY0FfcRnPOg";
    private static final String TOKEN = "pLO93fAqqk4YxwzfjB4ziiD2EM6m";
    private static final String INVALID_TOKEN = "throws401";
    private static final String TOO_MANY_REQUEST = "throws429";
    private static final String REFRESH_TOKEN = "k00PbiH2Eqk6FzT68wulSAblWUea4gVBNLjMbaM5WG";
    private static final String SCOPE = "psd2:account:balance:read+psd2:account:transaction:read+psd2:account:details:read";
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = prepareMeans();

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    private AbnAmroDataProvider dataProvider;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    @Qualifier("AbnAmroObjectMapper")
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplateManager restTemplateManager;

    Stream<AbnAmroDataProvider> getDataProviders() {
        return Stream.of(dataProvider);
    }

    @BeforeEach
    public void setUp() throws Exception {
        when(restTemplateManager.manage(any(RestTemplateManagerConfiguration.class))).thenReturn(restTemplateBuilder
                .rootUri("http://localhost:" + port)
                .build());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnProperProviderIdentifierForGetProviderIdentifier(AbnAmroDataProvider dataProvider) {
        // when
        String providerIdentifier = dataProvider.getProviderIdentifier();

        // then
        assertThat(providerIdentifier).isEqualTo("ABN_AMRO");

    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnProperProviderIdentifierDisplayNameForGetProviderIdentifierDisplayName(AbnAmroDataProvider dataProvider) {
        // when
        String providerIdentifierDisplayName = dataProvider.getProviderIdentifierDisplayName();

        // then
        assertThat(providerIdentifierDisplayName).isEqualTo("ABN AMRO");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnProperTypedAuthenticationMeansForGetTypedAuthenticationMeans(AbnAmroDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).containsOnlyKeys(CLIENT_ID_NAME, CLIENT_TRANSPORT_KEY_ID,
                CLIENT_TRANSPORT_CERTIFICATE, API_KEY_NAME);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnRedirectStepWithRedirectUrlForGetLoginInfoWithCorrectRequestData(AbnAmroDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL).setState("state")
                .setAuthenticationMeans(AUTH_MEANS)
                .build();

        // when
        RedirectStep step = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(step.getExternalConsentId()).isNull();
        assertThat(step.getProviderState()).isNull();
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(step.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/as/authorization.oauth2");
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        assertThat(queryParams.getFirst("client_id")).isEqualTo(CLIENT_ID);
        assertThat(queryParams.getFirst("response_type")).isEqualTo("code");
        assertThat(queryParams.getFirst("scope")).isEqualTo(SCOPE);
        assertThat(queryParams.getFirst("redirect_uri")).isEqualTo(REDIRECT_URL);
        assertThat(queryParams.getFirst("bank")).isEqualTo(BankConsentType.NLAA01.name());
        assertThat(queryParams.getFirst("state")).isEqualTo("state");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowGetAccessTokenFailedExceptionForCreateNewAccessMeansWhenUnsuccessfulRequest(AbnAmroDataProvider dataProvider) {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?abc=123&def=456&code=wrong_code") // checking URL stripping as well
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable createNewAccessMeansCallable = () -> dataProvider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(createNewAccessMeansCallable)
                .isInstanceOf(GetAccessTokenFailedException.class);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWithCorrectRequestData(AbnAmroDataProvider dataProvider) throws IOException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?abc=123&def=456&code=" + CODE) // checking URL stripping as well
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO accessMeans = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        AccessTokenResponseDTO accessTokenResponseDTO = objectMapper.readValue(accessMeans.getAccessMeans().getAccessMeans(), AccessTokenResponseDTO.class);
        assertThat(accessTokenResponseDTO.getAccessToken()).isEqualTo(TOKEN);
        assertThat(accessTokenResponseDTO.getTokenType()).isEqualTo("Bearer");
        assertThat(accessTokenResponseDTO.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(accessTokenResponseDTO.getScope()).isEqualTo(SCOPE.replace('+', ' '));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnNewAccessMeansForRefreshAccessMeansWithCorrectRequestData(AbnAmroDataProvider dataProvider) throws TokenInvalidException, IOException {
        // given
        String accessMeans = "{\n" +
                "      \"access_token\": \"pLO93fAqqk4YxwzfjB4ziiD2EM6m\",\n" +
                "      \"refresh_token\": \"k00PbiH2Eqk6FzT68wulSAblWUea4gVBNLjMbaM5WG\",\n" +
                "      \"scope\": \"psd2:account:balance:read psd2:account:transaction:read psd2:account:details:read\",\n" +
                "      \"token_type\": \"Bearer\",\n" +
                "      \"expires_in\": 7200\n" +
                "    }";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO obtained = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(obtained.getUserId()).isEqualTo(USER_ID);
        AccessTokenResponseDTO accessTokenResponseDTO = objectMapper.readValue(obtained.getAccessMeans(), AccessTokenResponseDTO.class);
        assertThat(accessTokenResponseDTO.getAccessToken()).isEqualTo(TOKEN);
        assertThat(accessTokenResponseDTO.getTokenType()).isEqualTo("Bearer");
        assertThat(accessTokenResponseDTO.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowTokenInvalidExceptionAfterReceiving401WhileFetchindData(AbnAmroDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlFetchDataRequest request = getUrlFetchDataRequest(INVALID_TOKEN);
        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);
        // then
        assertThatThrownBy(fetchDataCallable).isInstanceOf(TokenInvalidException.class);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldThrowBackPressureRequestExceptionAfterReceiving429WhileFetchingData(AbnAmroDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlFetchDataRequest request = getUrlFetchDataRequest(TOO_MANY_REQUEST);
        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(request);
        // then
        assertThatThrownBy(fetchDataCallable).isInstanceOf(BackPressureRequestException.class).hasMessageContaining("429 Too Many Requests");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnDataForFetchDataWithCorrectRequestData(AbnAmroDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException {
        // given
        UrlFetchDataRequest request = getUrlFetchDataRequest(TOKEN);
        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        ProviderAccountDTO expectedAccount = expectedAccount();
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0)).isEqualToIgnoringGivenFields(expectedAccount, "transactions", "lastRefreshed");

        // check that account is valid (it involves validating inner transactions as well)
        accounts.get(0).validate();

        List<ProviderTransactionDTO> transactions = accounts.get(0).getTransactions();
        assertThat(transactions).hasSize(39);
        assertThat(transactions.get(0)).isEqualToIgnoringGivenFields(expectedAccount.getTransactions().get(0),
                "extendedTransaction");
        assertThat(transactions.get(38)).isEqualToIgnoringGivenFields(expectedAccount.getTransactions().get(1),
                "extendedTransaction", "transactionId");

        assertThat(transactions.get(0).getExtendedTransaction()).isEqualTo(
                expectedAccount.getTransactions().get(0).getExtendedTransaction());
        assertThat(transactions.get(38).getExtendedTransaction()).isEqualToIgnoringGivenFields(
                expectedAccount.getTransactions().get(1).getExtendedTransaction(), "transactionId");

        assertThat(transactions.get(1).getExtendedTransaction().getDebtorName()).isEqualTo("Nederlandse Loterij Organisatie B.V.");
        assertThat(transactions.get(1).getMerchant()).isEqualTo("Nederlandse Loterij Organisatie B.V.");
        assertThat(transactions.get(5).getExtendedTransaction().getCreditorName()).isEqualTo("Gardencafe ING 1460 AMST");
        assertThat(transactions.get(20).getExtendedTransaction().getCreditorName()).isEqualTo("AAB RETAIL INZ TIKKIE");
        assertThat(transactions.get(21).getExtendedTransaction().getDebtorName()).isEqualTo("Hr PH de Vries, Hr M Jansen-Janszoon");
        assertThat(transactions.get(32).getExtendedTransaction().getCreditorName()).isEqualTo("Has counter party name, but no Naam: description line");
        assertThat(transactions.get(33).getExtendedTransaction().getCreditorName()).isEqualTo("Zero description lines");
        assertThat(transactions.get(34).getExtendedTransaction().getCreditorName()).isEqualTo("Null description lines");
        assertThat(transactions.get(35).getExtendedTransaction().getCreditorName()).isEqualTo("Gardencaferegexttest ING 1460 AMST");
        assertThat(transactions.get(37).getExtendedTransaction().getCreditorName()).isEqualTo("Mr ABC anonymous");
    }

    private UrlFetchDataRequest getUrlFetchDataRequest(String token) throws JsonProcessingException {
        AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();
        accessTokenResponseDTO.setAccessToken(token);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(accessTokenResponseDTO), new Date(), new Date());
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private static Map<String, BasicAuthenticationMean> prepareMeans() {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(CLIENT_ID_NAME, new BasicAuthenticationMean(API_KEY_STRING.getType(), CLIENT_ID));
        means.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID.toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificates()));
        means.put(API_KEY_NAME, new BasicAuthenticationMean(API_KEY_STRING.getType(), API_KEY));
        return means;
    }

    private static String readCertificates() {
        try {
            URI fileURI = AbnAmroDataProviderIntegrationTest.class
                    .getClassLoader()
                    .getResource("certificates/yolt_certificate.pem")
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static ProviderAccountDTO expectedAccount() {
        List<BalanceDTO> balanceDTOs = new ArrayList<>();
        balanceDTOs.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_BOOKED)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("16.03"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .build());

        ExtendedAccountDTO extendedAccountDTO = ExtendedAccountDTO.builder()
                .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value("NL12ABNA9999876523")
                        .build()))
                .balances(balanceDTOs)
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .currency(CurrencyCode.EUR)
                .name("Current Account")
                .build();

        ExtendedTransactionDTO extendedTransaction1 = ExtendedTransactionDTO.builder()
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("2468.04"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("SEPA Overboeking, IBAN: DE11140499990008401194")
                .bookingDate(LocalDateTime.parse("2019-08-20-04:16:37.410", DateTimeFormatter.ofPattern(TransactionTimestampDateExtractor.TRANSACTION_TIMESTAMP_PATTERN)).atZone(TransactionTimestampDateExtractor.AMSTERDAM_TIMEZONE))
                .debtorName("Opdrachtgever 168")
                .debtorAccount(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value("DE11140499990008401194")
                        .build())
                .status(TransactionStatus.BOOKED)
                .build();

        ExtendedTransactionDTO extendedTransaction2 = ExtendedTransactionDTO.builder()
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(new BigDecimal("-100.19"))
                        .currency(CurrencyCode.EUR)
                        .build())
                .remittanceInformationUnstructured("SEPA Overboeking, IBAN: NL61ABNA9999180891")
                .bookingDate(LocalDateTime.parse("2019-08-15-04:16:37.410", DateTimeFormatter.ofPattern(TransactionTimestampDateExtractor.TRANSACTION_TIMESTAMP_PATTERN)).atZone(TransactionTimestampDateExtractor.AMSTERDAM_TIMEZONE))
                .creditorName("Opdrachtgever 181")
                .creditorAccount(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value("NL54ABNA9998700418")
                        .build())
                .status(TransactionStatus.BOOKED)
                .build();

        List<ProviderTransactionDTO> transactionDTOs = new ArrayList<>();

        transactionDTOs.add(ProviderTransactionDTO.builder()
                .amount(new BigDecimal("2468.04"))
                .category(YoltCategory.GENERAL)
                .dateTime(LocalDateTime.parse("2019-08-20-04:16:37.410", DateTimeFormatter.ofPattern(TransactionTimestampDateExtractor.TRANSACTION_TIMESTAMP_PATTERN)).atZone(TransactionTimestampDateExtractor.AMSTERDAM_TIMEZONE))
                .description("SEPA Overboeking, IBAN: DE11140499990008401194")
                .extendedTransaction(extendedTransaction1)
                .merchant("Opdrachtgever 168")
                .externalId("9855H1218554877S0AB")
                .type(ProviderTransactionType.CREDIT)
                .status(TransactionStatus.BOOKED)
                .build());

        transactionDTOs.add(ProviderTransactionDTO.builder()
                .amount(new BigDecimal("100.19"))
                .category(YoltCategory.GENERAL)
                .dateTime(LocalDateTime.parse("2019-08-15-04:16:37.410", DateTimeFormatter.ofPattern(TransactionTimestampDateExtractor.TRANSACTION_TIMESTAMP_PATTERN)).atZone(TransactionTimestampDateExtractor.AMSTERDAM_TIMEZONE))
                .description("SEPA Overboeking, IBAN: NL61ABNA9999180891")
                .extendedTransaction(extendedTransaction2)
                .merchant("Opdrachtgever 181")
                .type(ProviderTransactionType.DEBIT)
                .status(TransactionStatus.BOOKED)
                .build());

        ProviderAccountNumberDTO accountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "NL12ABNA9999876523");
        accountNumberDTO.setHolderName("Commercial Client");

        return ProviderAccountDTO.builder()
                .accountId(HashUtils.sha256Hash("NL12ABNA9999876523"))
                .accountNumber(accountNumberDTO)
                .currency(CurrencyCode.EUR)
                .currentBalance(new BigDecimal("16.03"))
                .extendedAccount(extendedAccountDTO)
                .lastRefreshed(Instant.now().atZone(ZoneOffset.UTC))
                .name("Current Account")
                .transactions(transactionDTOs)
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .build();
    }
}
