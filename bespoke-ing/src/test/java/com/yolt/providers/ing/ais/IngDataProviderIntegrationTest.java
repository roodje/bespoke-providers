package com.yolt.providers.ing.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.ing.TestApp;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.TestSigner;
import com.yolt.providers.ing.common.auth.IngAuthData;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.auth.IngUserAccessMeans;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.dto.TestIngAuthData;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.ing.common.auth.IngAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow", httpsPort = 0, port = 0)
public class IngDataProviderIntegrationTest {

    private static final String CLIENT_ID = "example_client_id";
    private static final String REDIRECT_URI = "xxx";
    private static final String STATE = "state";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CODE = "694d6ca9-1310-4d83-8dbb-e819c1ee6b80";
    private static final String REDIRECT_SCOPE = "payment-accounts%3Abalances%3Aview%20payment-accounts%3Atransactions%3Aview";
    private static final String SCOPE = "payment-accounts:transactions:view payment-accounts:balances:view";

    private static final String EXPECTED_ACCESS_TOKEN = "test-customer-access-token";
    private static final String EXPECTED_REFRESH_TOKEN = "test-customer-refresh-token";
    private static final String EXPIRED_ACCESS_TOKEN = "test-customer-expired-access-token";

    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());

    private static final String BANK_SPECIFIC_TRANSACTION_TYPE = "transactionType";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("IngDeDataProviderV10")
    private IngDataProviderV9 ingDeDataProviderV10;

    @Autowired
    @Qualifier("IngDeDataProviderV11")
    private IngDataProviderV9 ingDeDataProviderV11;

    @Autowired
    @Qualifier("IngBeDataProviderV10")
    private IngDataProviderV9 ingBeDataProviderV10;

    @Autowired
    @Qualifier("IngFrDataProviderV10")
    private IngDataProviderV9 ingFrDataProviderV10;

    @Autowired
    @Qualifier("IngItDataProviderV10")
    private IngDataProviderV9 ingItDataProviderV10;

    @Autowired
    @Qualifier("IngNlDataProviderV10")
    private IngDataProviderV9 ingNlDataProviderV10;

    @Autowired
    @Qualifier("IngRoDataProviderV10")
    private IngDataProviderV9 ingRoDataProviderV10;

    @Autowired
    private Clock clock;

    private ObjectMapper mapper;

    private RestTemplateManager restTemplateManager;
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        mapper = IngObjectMapper.get();
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_signing.cer")));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_tls.cer")));
        authenticationMeans.put(TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "00000000-0000-0000-0000-000000000000"));
        authenticationMeans.put(SIGNING_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
        PrivateKey signingKey = KeyUtil.createPrivateKeyFromPemFormat((loadPemFile("example_client_signing.key")));
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);
        restTemplateManager = new SimpleRestTemplateManagerMock(externalRestTemplateBuilderFactory);
        signer = new TestSigner(signingKey);
    }

    public Stream<Arguments> getIngDataProviders() {
        return Stream.of(
                arguments(ingBeDataProviderV10.getProviderIdentifier(),
                        ingBeDataProviderV10,
                        ZoneId.of("Europe/Brussels"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Brussels")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Brussels"))),
                arguments(ingItDataProviderV10.getProviderIdentifier(),
                        ingItDataProviderV10,
                        ZoneId.of("Europe/Rome"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Brussels")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Brussels"))),
                arguments(ingFrDataProviderV10.getProviderIdentifier(),
                        ingFrDataProviderV10,
                        ZoneId.of("Europe/Paris"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Brussels")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Brussels"))),
                arguments(ingNlDataProviderV10.getProviderIdentifier(),
                        ingNlDataProviderV10,
                        ZoneId.of("Europe/Amsterdam"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Brussels")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Brussels"))),
                arguments(ingRoDataProviderV10.getProviderIdentifier(),
                        ingRoDataProviderV10,
                        ZoneId.of("Europe/Bucharest"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Brussels")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Brussels"))),
                arguments(ingDeDataProviderV10.getProviderIdentifier(),
                        ingDeDataProviderV10,
                        ZoneId.of("Europe/Berlin"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Berlin")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Berlin"))),
                arguments(ingDeDataProviderV11.getProviderIdentifier(),
                        ingDeDataProviderV11,
                        ZoneId.of("Europe/Berlin"),
                        ZonedDateTime.of(2020, 8, 30, 2, 2, 45, 0, ZoneId.of("Europe/Berlin")),
                        ZonedDateTime.of(2018, 7, 1, 11, 16, 54, 991000000, ZoneId.of("Europe/Berlin")))
        );
    }

    @ParameterizedTest
    @MethodSource("getIngDataProviders")
    public void shouldReturnDataForFetchDataWithCorrectRequestData(String identifier,
                                                                   IngDataProviderV9 subject,
                                                                   ZoneId expectedZone,
                                                                   ZonedDateTime valueDate,
                                                                   ZonedDateTime latestChangeDateTime) throws ProviderFetchDataException, JsonProcessingException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(UUID.randomUUID(),
                mapper.writeValueAsString(prepareProperUserAccessMeans()),
                Date.from(ZonedDateTime.now(expectedZone).plusDays(7).toInstant()),
                Date.from(ZonedDateTime.now(expectedZone).plusDays(14).toInstant()));
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Clock.systemUTC().instant())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        DataProviderResponse response = subject.fetchData(urlFetchDataRequest);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertAccountsMatchJson(accounts, expectedZone, latestChangeDateTime);
        assertTransactionsMatchJson(accounts, expectedZone, valueDate);
    }

    @ParameterizedTest
    @MethodSource("getIngDataProviders")
    public void shouldReturnRedirectStepWithRedirectUrlForGetLoginInfoWithCorrectRequestData(String identifier,
                                                                                             IngDataProviderV9 subject) {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URI).setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();
        String expectedRedirectUrl = "http://localhost:8888/yoltbank/ing/granting/1234567/xx?" +
                "client_id=" + CLIENT_ID + "&scope=" + REDIRECT_SCOPE + "&state=" + STATE + "&redirect_uri=" +
                REDIRECT_URI + "" + "&response_type=code";

        // when
        RedirectStep redirectStep = subject.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(expectedRedirectUrl);
    }

    @ParameterizedTest
    @MethodSource("getIngDataProviders")
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWithCorrectRequestData(String identifier,
                                                                                        IngDataProviderV9 subject) throws JsonProcessingException {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URI + "?code=" + CODE)
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState(mapper.writeValueAsString(prepareProperClientAccessMeans()))
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStep = subject.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AccessMeansDTO accessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(accessMeans.getAccessMeans()).isNotNull();
        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        IngUserAccessMeans ingAccessMeans = mapper.readValue(accessMeans.getAccessMeans(), IngUserAccessMeans.class);
        assertThat(ingAccessMeans.getAccessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
        assertThat(ingAccessMeans.getTokenType()).isEqualTo("access");
        assertThat(ingAccessMeans.getRefreshToken()).isEqualTo(EXPECTED_REFRESH_TOKEN);
        assertThat(ingAccessMeans.getScope()).isEqualTo(SCOPE);
    }

    @ParameterizedTest
    @MethodSource("getIngDataProviders")
    public void shouldReturnNewAccessMeansForRefreshAccessMeansWithCorrectRequestData(String identifier,
                                                                                      IngDataProviderV9 subject) throws JsonProcessingException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, mapper.writeValueAsString(prepareExpiredUserAccessMeans()), new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO newAccessMeans = subject.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(newAccessMeans.getAccessMeans()).isNotNull();
        assertThat(newAccessMeans.getUserId()).isEqualTo(USER_ID);
        IngUserAccessMeans convertedAccessMeans = mapper.readValue(newAccessMeans.getAccessMeans(), IngUserAccessMeans.class);
        assertThat(convertedAccessMeans.getClientAccessMeans().getClientId()).isEqualTo(CLIENT_ID);
        assertThat(convertedAccessMeans.getAccessToken()).isEqualTo(EXPECTED_ACCESS_TOKEN);
        assertThat(convertedAccessMeans.getTokenType()).isEqualTo("Bearer");
        assertThat(convertedAccessMeans.getScope()).isEqualTo(SCOPE);
    }

    private String loadPemFile(final String fileName) throws IOException {
        URI uri = resourceLoader.getResource("classpath:certificates/" + fileName).getURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

    private void assertAccountsMatchJson(List<ProviderAccountDTO> accounts,
                                         ZoneId expectedZone,
                                         ZonedDateTime latestChangeDateTime) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(accounts).hasSize(2);

        ProviderAccountDTO account1 = accounts.get(0);
        softly.assertThat(account1.getTransactions()).hasSize(3);
        softly.assertThat(account1.getAccountId()).isEqualTo("7de0041d-4f25-4b6c-a885-0bbeb1eab220");
        softly.assertThat(account1.getName()).isEqualTo("SANTINO");
        softly.assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        softly.assertThat(account1.getAvailableBalance()).isEqualTo("547.60");
        softly.assertThat(account1.getCurrentBalance()).isEqualTo("547.60");
        softly.assertThat(account1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        softly.assertThat(account1.getCreditCardData()).isNull();

        ProviderAccountNumberDTO accountNumber1 = account1.getAccountNumber();
        softly.assertThat(accountNumber1).isNotNull();
        softly.assertThat(accountNumber1.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        softly.assertThat(accountNumber1.getIdentification()).isEqualTo("IT50N1234561605000323304444");
        softly.assertThat(accountNumber1.getHolderName()).isEqualTo("SANTINO");

        ExtendedAccountDTO extendedAccount1 = account1.getExtendedAccount();
        softly.assertThat(extendedAccount1).isNotNull();
        softly.assertThat(extendedAccount1.getResourceId()).isEqualTo("7de0041d-4f25-4b6c-a885-0bbeb1eab220");
        softly.assertThat(extendedAccount1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        softly.assertThat(extendedAccount1.getName()).isEqualTo("SANTINO");
        softly.assertThat(account1.getExtendedAccount().getProduct()).isEqualTo("Conto Arancio");
        softly.assertThat(extendedAccount1.getBalances()).hasSize(1);

        List<BalanceDTO> balances1 = extendedAccount1.getBalances();
        balances1.forEach(balance -> {
            softly.assertThat(balance).isNotNull();
            softly.assertThat(balance.getBalanceType()).isEqualTo(BalanceType.EXPECTED);
            softly.assertThat(balance.getReferenceDate().getZone()).isEqualTo(expectedZone);
            softly.assertThat(balance.getReferenceDate().toLocalDate()).isEqualTo(LocalDate.of(2020, 10, 7));
            softly.assertThat(balance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
            softly.assertThat(balance.getBalanceAmount().getAmount()).isIn(new BigDecimal("547.60"), new BigDecimal("50.12"));
        });

        ProviderAccountDTO account2 = accounts.get(1);
        softly.assertThat(account2.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        softly.assertThat(account2.getCreditCardData()).isEqualTo(new ProviderCreditCardDTO());
        softly.assertThat(account2.getAccountMaskedIdentification()).isEqualTo("123456******6789");

        softly.assertAll();
    }

    private void assertTransactionsMatchJson(List<ProviderAccountDTO> accounts,
                                             ZoneId expectedZone,
                                             ZonedDateTime transactionDateTime) {
        List<ProviderTransactionDTO> transactions1 = accounts.get(0).getTransactions();
        SoftAssertions softly = new SoftAssertions();

        ProviderTransactionDTO transaction1_1 = transactions1.get(0);
        softly.assertThat(transaction1_1.getDateTime()).isEqualTo(transactionDateTime);
        softly.assertThat(transaction1_1.getDateTime().getZone()).isEqualTo(expectedZone);
        softly.assertThat(transaction1_1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        softly.assertThat(transaction1_1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        softly.assertThat(transaction1_1.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        softly.assertThat(transaction1_1.getAmount()).isEqualTo("5");
        softly.assertThat(transaction1_1.getDescription()).isEqualTo("HAFO DEN HAAG SGRAVENHAGE NLD\nPasvolgnr: 111 22-01-2022 15:17\nTransactie: 30C5S4 Term: 000001\nValutadatum: 23-01-2022");
        softly.assertThat(transaction1_1.getBankSpecific()).containsKey(BANK_SPECIFIC_TRANSACTION_TYPE).containsValue("Overschrijving");

        ExtendedTransactionDTO extendedTransaction1_1 = transaction1_1.getExtendedTransaction();
        softly.assertThat(extendedTransaction1_1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        softly.assertThat(extendedTransaction1_1.getDebtorName()).isEqualTo("Debtor Name");
        softly.assertThat(extendedTransaction1_1.getCreditorAccount()).isNull();
        softly.assertThat(extendedTransaction1_1).isNotNull();
        softly.assertThat(extendedTransaction1_1.getEndToEndId()).isEqualTo("NOTPROVIDED");
        softly.assertThat(extendedTransaction1_1.getBookingDate().getZone()).isEqualTo(expectedZone);
        softly.assertThat(extendedTransaction1_1.getBookingDate()).isEqualTo(ZonedDateTime.of(2020, 8, 30, 0, 0, 0, 0, expectedZone));
        softly.assertThat(extendedTransaction1_1.getValueDate().getZone()).isEqualTo(expectedZone);
        softly.assertThat(extendedTransaction1_1.getValueDate()).isEqualTo(ZonedDateTime.of(2020, 8, 30, 0, 0, 0, 0, expectedZone));
        softly.assertThat(extendedTransaction1_1.getRemittanceInformationUnstructured()).isEqualTo("HAFO DEN HAAG SGRAVENHAGE NLD<br>Pasvolgnr: 111 22-01-2022 15:17<br>Transactie: 30C5S4 Term: 000001<br>Valutadatum: 23-01-2022");
        softly.assertThat(extendedTransaction1_1.getRemittanceInformationStructured()).isEqualTo("RF18539007547034");

        BalanceAmountDTO balance1_1 = extendedTransaction1_1.getTransactionAmount();
        softly.assertThat(balance1_1).isNotNull();
        softly.assertThat(balance1_1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        softly.assertThat(balance1_1.getAmount()).isEqualTo("5");

        AccountReferenceDTO debtorAccount1_1 = extendedTransaction1_1.getDebtorAccount();
        softly.assertThat(debtorAccount1_1).isNotNull();
        softly.assertThat(debtorAccount1_1.getType()).isEqualTo(AccountReferenceType.BBAN);
        softly.assertThat(debtorAccount1_1.getValue()).isEqualTo("INGB1234426876");

        ProviderTransactionDTO transaction1_2 = transactions1.get(1);
        softly.assertThat(transaction1_2.getStatus()).isEqualTo(TransactionStatus.PENDING);
        softly.assertThat(transaction1_2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        softly.assertThat(transaction1_2.getAmount()).isEqualTo("47.13");
        softly.assertThat(transaction1_2.getDescription()).isEqualTo("Naam: T-mobile Netherlands\nOmschrijving: 0000071511/102840\nIBAN: NL87COBA1111000000\nKenmerk: Betaling toestel\nValutadatum: 20-01-2022");
        softly.assertThat(transaction1_2.getBankSpecific()).containsKey(BANK_SPECIFIC_TRANSACTION_TYPE).containsValue("Betaalautomaat");

        ExtendedTransactionDTO extendedTransaction1_2 = transaction1_2.getExtendedTransaction();
        softly.assertThat(extendedTransaction1_2.getStatus()).isEqualTo(TransactionStatus.PENDING);
        softly.assertThat(extendedTransaction1_2.getCreditorName()).isEqualTo("Creditor Name");
        softly.assertThat(extendedTransaction1_2.getDebtorAccount()).isNull();
        softly.assertThat(extendedTransaction1_2.getRemittanceInformationStructured()).isEqualTo(null);
        softly.assertThat(extendedTransaction1_2.getRemittanceInformationUnstructured()).isEqualTo("Naam: T-mobile Netherlands<br>Omschrijving: 0000071511/102840<br>IBAN: NL87COBA1111000000<br>Kenmerk: Betaling toestel<br>Valutadatum: 20-01-2022");

        BalanceAmountDTO balance1_2 = extendedTransaction1_2.getTransactionAmount();
        softly.assertThat(balance1_2.getAmount()).isEqualTo("-47.13");

        AccountReferenceDTO creditorAccount1_2 = extendedTransaction1_2.getCreditorAccount();
        softly.assertThat(creditorAccount1_2).isNotNull();
        softly.assertThat(creditorAccount1_2.getType()).isEqualTo(AccountReferenceType.IBAN);
        softly.assertThat(creditorAccount1_2.getValue()).isNull();

        ProviderTransactionDTO transaction1_3 = transactions1.get(2);
        softly.assertThat(transaction1_3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        softly.assertThat(transaction1_3.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        softly.assertThat(transaction1_3.getAmount()).isEqualTo("15");
        softly.assertThat(transaction1_3.getDescription()).isEqualTo("Naam: Hr N van der Niels\nIBAN: NL52INGB0001111111\nDatum/Tijd: 08-01-2022 14:00:36\nValutadatum: 08-01-2022");
        softly.assertThat(transaction1_3.getBankSpecific()).containsKey(BANK_SPECIFIC_TRANSACTION_TYPE).containsValue("Online bankieren");

        ExtendedTransactionDTO extendedTransaction1_3 = transaction1_3.getExtendedTransaction();
        softly.assertThat(extendedTransaction1_3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        softly.assertThat(extendedTransaction1_3.getDebtorName()).isEqualTo("Debtor Name");
        softly.assertThat(extendedTransaction1_3.getCreditorAccount()).isNull();
        softly.assertThat(extendedTransaction1_3.getRemittanceInformationUnstructured()).isEqualTo("Naam: Hr N van der Niels<br>IBAN: NL52INGB0001111111<br>Datum/Tijd: 08-01-2022 14:00:36<br>Valutadatum: 08-01-2022");

        BalanceAmountDTO balance1_3 = extendedTransaction1_3.getTransactionAmount();
        softly.assertThat(balance1_3.getAmount()).isEqualTo("15");

        AccountReferenceDTO debtorAccount = extendedTransaction1_3.getDebtorAccount();
        softly.assertThat(debtorAccount).isNotNull();
        softly.assertThat(debtorAccount.getType()).isEqualTo(AccountReferenceType.IBAN);
        softly.assertThat(debtorAccount.getValue()).isEqualTo("NL10INGB5432100114");

        List<ProviderTransactionDTO> transactions2 = accounts.get(1).getTransactions();

        ProviderTransactionDTO transaction2_1 = transactions2.get(0);
        softly.assertThat(transaction2_1.getDateTime()).isEqualTo(ZonedDateTime.of(2020, 8, 28, 0, 0, 0, 0, expectedZone));
        softly.assertThat(transaction2_1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        softly.assertThat(transaction2_1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        softly.assertThat(transaction2_1.getAmount()).isEqualTo("10.01");
        softly.assertThat(transaction2_1.getBankSpecific()).isNull();

        ExtendedTransactionDTO extendedTransaction2_1 = transaction2_1.getExtendedTransaction();
        softly.assertThat(extendedTransaction2_1.getBookingDate()).isEqualTo(ZonedDateTime.of(2020, 8, 28, 0, 0, 0, 0, expectedZone));
        softly.assertThat(extendedTransaction2_1.getValueDate()).isEqualTo(ZonedDateTime.of(2020, 8, 28, 0, 0, 0, 0, expectedZone));
        softly.assertThat(extendedTransaction2_1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        softly.assertThat(extendedTransaction2_1.getCreditorAccount()).isNull();

        BalanceAmountDTO balance2_1 = extendedTransaction2_1.getTransactionAmount();
        assertThat(balance2_1.getAmount()).isEqualTo("-10.01");

        AccountReferenceDTO debtorAccount2_1 = extendedTransaction2_1.getDebtorAccount();
        softly.assertThat(debtorAccount2_1).isNull();

        ProviderTransactionDTO transaction2_2 = transactions2.get(1);
        softly.assertThat(transaction2_2.getStatus()).isEqualTo(TransactionStatus.PENDING);
        softly.assertThat(transaction2_2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        softly.assertThat(transaction2_2.getAmount()).isEqualTo("107.88");
        softly.assertThat(transaction2_2.getBankSpecific()).isNull();

        ExtendedTransactionDTO extendedTransaction2_2 = transaction2_2.getExtendedTransaction();
        softly.assertThat(extendedTransaction2_2.getStatus()).isEqualTo(TransactionStatus.PENDING);
        softly.assertThat(extendedTransaction2_2.getDebtorAccount()).isNull();

        BalanceAmountDTO balance2_2 = extendedTransaction2_2.getTransactionAmount();
        softly.assertThat(balance2_2.getAmount()).isEqualTo("-107.88");

        AccountReferenceDTO creditorAccount2_2 = extendedTransaction2_2.getCreditorAccount();
        softly.assertThat(creditorAccount2_2).isNull();

        softly.assertAll();
    }

    private IngClientAccessMeans prepareProperClientAccessMeans() {
        return new IngClientAccessMeans(prepareProperTokenResponse(), AUTHENTICATION_MEANS_REFERENCE, clock);
    }

    private IngUserAccessMeans prepareProperUserAccessMeans() {
        IngAuthData tokenResponse = prepareProperTokenResponse();
        return new IngUserAccessMeans(tokenResponse, new IngClientAccessMeans(tokenResponse, AUTHENTICATION_MEANS_REFERENCE, clock), clock);
    }

    private IngAuthData prepareProperTokenResponse() {
        TestIngAuthData tokenResponse = new TestIngAuthData();
        tokenResponse.setAccessToken(EXPECTED_ACCESS_TOKEN);
        tokenResponse.setRefreshToken(EXPECTED_REFRESH_TOKEN);
        tokenResponse.setClientId(CLIENT_ID);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(0L);
        tokenResponse.setRefreshTokenExpiresIn(0L);
        return tokenResponse;
    }

    private IngAuthData prepareExpiredTokenResponse() {
        TestIngAuthData tokenResponse = new TestIngAuthData();
        tokenResponse.setAccessToken(EXPIRED_ACCESS_TOKEN);
        tokenResponse.setRefreshToken(EXPECTED_REFRESH_TOKEN);
        tokenResponse.setClientId(CLIENT_ID);
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(0L);
        tokenResponse.setRefreshTokenExpiresIn(0L);
        return tokenResponse;
    }

    private IngUserAccessMeans prepareExpiredUserAccessMeans() {
        IngAuthData tokenResponse = prepareExpiredTokenResponse();
        return new IngUserAccessMeans(tokenResponse, new IngClientAccessMeans(tokenResponse, AUTHENTICATION_MEANS_REFERENCE, clock), clock);
    }
}
