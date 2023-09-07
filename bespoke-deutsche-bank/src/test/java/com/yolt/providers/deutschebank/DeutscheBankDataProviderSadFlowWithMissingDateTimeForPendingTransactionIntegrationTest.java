package com.yolt.providers.deutschebank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.deutschebank.common.DeutscheBankGroupDataProviderV2;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import com.yolt.providers.deutschebank.de.DeutscheBankDataProviderV1;
import com.yolt.providers.deutschebank.es.DeutscheBankEsDataProviderV1;
import com.yolt.providers.deutschebank.postbank.PostbankDataProviderV1;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1.TRANSPORT_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sad-flow/missing-datetime-for-pending-transaction", httpsPort = 0, port = 0)
@ActiveProfiles("deutschebank")
class DeutscheBankDataProviderSadFlowWithMissingDateTimeForPendingTransactionIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-01");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    private static final String CONSENT_ID = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";

    @Autowired
    @Qualifier("DeutscheBankGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private DeutscheBankDataProviderV1 deutscheBankDataProviderV1;

    @Autowired
    private PostbankDataProviderV1 postbankDataProviderV1;

    @Autowired
    private DeutscheBankEsDataProviderV1 deutscheBankEsDataProviderV1;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private DeutscheBankGroupProviderStateMapper providerStateMapper;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        providerStateMapper = new DeutscheBankGroupProviderStateMapper(objectMapper);
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
    }

    UrlDataProvider[] getProviders() {
        return new UrlDataProvider[]{deutscheBankDataProviderV1, postbankDataProviderV1, deutscheBankEsDataProviderV1};
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnAccountsAndTransactions(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(1);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1", "510.10", "510.10"));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(4);

        assertThat(account1Transactions)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "dateTime",
                        "description",
                        "category",
                        "merchant",
                        "extendedTransaction",
                        "bankSpecific"
                )
                .containsExactlyInAnyOrder(
                        ProviderTransactionDTO.builder()
                                .externalId("000001-330DA1CD87C588578AD4906D5C1E11127151742020-10-03 12:05:17.4263780")
                                .amount(new BigDecimal("0.13"))
                                .dateTime(createExpectedDateTimeInBerlinZone("2020-10-03"))
                                .status(PENDING)
                                .type(DEBIT)
                                .build(),
                        ProviderTransactionDTO.builder()
                                .externalId("000002-330DA1CD87C58X5S8AF4E06DC1EE11127151742020-10-03 09:05:17.426")
                                .amount(new BigDecimal("95.415"))
                                .dateTime(createExpectedDateTimeInBerlinZone("2020-10-03"))
                                .status(PENDING)
                                .type(CREDIT)
                                .build(),
                        ProviderTransactionDTO.builder()
                                .externalId("000001-314962621")
                                .amount(new BigDecimal("265.54"))
                                .dateTime(ZonedDateTime.now(Clock.system(ZoneId.of("Europe/Berlin"))))
                                .status(PENDING)
                                .type(DEBIT)
                                .build(),
                        ProviderTransactionDTO.builder()
                                .externalId("328CHDP182190065")
                                .amount(new BigDecimal("400.00"))
                                .dateTime(createExpectedDateTimeInBerlinZone("2020-10-03"))
                                .status(BOOKED)
                                .type(DEBIT)
                                .build()
                );
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId, String availableBalance, String currentBalance) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isNotEmpty();
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isNotEmpty();

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId,
                                                                            String amount,
                                                                            ZonedDateTime dateTime,
                                                                            TransactionStatus status,
                                                                            ProviderTransactionType type) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            assertThat(providerTransactionDTO.getDateTime()).isCloseTo(dateTime, within(1, DAYS));
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(extendedTransactionDTO.getBookingDate()).isNotNull();
            assertThat(extendedTransactionDTO.getValueDate()).isNotNull();
            assertThat(extendedTransactionDTO.getValueDate()).isNotNull();
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isNotEmpty();

            BalanceAmountDTO balanceAmountDTO = extendedTransactionDTO.getTransactionAmount();
            if (DEBIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount).negate());
            }
            if (CREDIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        };
    }

    private AccessMeansDTO createAccessMeansDTO() {
        DeutscheBankGroupProviderState providerState = createProviderState();
        return new AccessMeansDTO(USER_ID, providerStateMapper.toJson(providerState), UPDATED_DATE, EXPIRATION_DATE);
    }

    private DeutscheBankGroupProviderState createProviderState() {
        return new DeutscheBankGroupProviderState(CONSENT_ID);
    }

    private ZonedDateTime createExpectedDateTimeInBerlinZone(String dateTime) {
        return LocalDate.parse(dateTime).atStartOfDay(ZoneId.of("Europe/Berlin"));
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}
