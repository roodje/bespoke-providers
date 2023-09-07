package com.yolt.providers.axabanque.common;

import com.yolt.providers.axabanque.common.fixtures.AuthMeansFixture;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/v1/fetchdata/default/", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultFetchDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TRANSPORT_KEY_ID_VALUE = UUID.randomUUID().toString();
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = AuthMeansFixture.getAuthMeansMap(TRANSPORT_KEY_ID_VALUE);
    private static final String PST_IP_ADDRESS = "127.0.1.2";
    private static final ZoneId zoneId = ZoneId.of("Europe/Paris");

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    @Qualifier("AxaBeDataProviderV1")
    private UrlDataProvider axaBeDataProviderV1;

    @Autowired
    @Qualifier("BpostDataProviderV1")
    private UrlDataProvider bpostBeDataProviderV1;

    @Autowired
    @Qualifier("CrelanDataProviderV1")
    private UrlDataProvider crelanDataProviderV1;

    @Autowired
    @Qualifier("KeytradeDataProviderV1")
    private UrlDataProvider keytradeDataProviderV1;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(axaBeDataProviderV1, bpostBeDataProviderV1, crelanDataProviderV1, keytradeDataProviderV1);
    }

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Mock
    private Signer signer;

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldSuccessfullyFetchDataAndMapDataCorrectly(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        long consentGeneratedNow = Instant.now(Clock.system(zoneId)).toEpochMilli();
        String accessMeans = "{\"baseRedirectUri\":\"https:baseUri.com\",\"providerState\":{\"codeVerifier\":\"codeVerifier\",\"code\":\"code\",\"consentId\":\"consentId\",\"traceId\":\"traceId\",\"consentGeneratedAt\":" + consentGeneratedNow + "},\"accessToken\":{\"expiresIn\":900,\"refreshToken\":\"THE-REFRESH-TOKEN\",\"scope\":\"token scope\",\"tokenType\":\"Bearer\",\"token\":\"THE-ACCESS-TOKEN\"}}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(AUTH_MEANS)
                .setSigner(signer)
                .setTransactionsFetchStartTime(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PST_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then

        assertThat(response.getAccounts()).hasSize(2);
        //account0
        ProviderAccountDTO account0 = response.getAccounts().stream()
                .filter(a -> a.getAccountId().equals("CTR|1271339|12345678901|1|2|1|12345678901"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No account found"));
        assertThat(account0.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account0.getAvailableBalance()).isEqualTo("19868.38");
        assertThat(account0.getCurrentBalance()).isEqualTo("19868.38");
        assertThat(account0.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account0.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account0.getAccountNumber().getIdentification()).isEqualTo("FR7612548029981234567890122");
        assertThat(account0.getTransactions()).hasSize(4);
        ProviderTransactionDTO transaction00 = account0.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("9.95")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction00.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-10-27").atStartOfDay(zoneId)));
        assertThat(transaction00.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction00.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction00.getDescription()).isEqualTo("CB AUDIBLE.FR");
        ExtendedTransactionDTO extendedTransaction00 = transaction00.getExtendedTransaction();
        assertThat(extendedTransaction00.getValueDate()).isEqualTo("2020-10-27T00:00+01:00[Europe/Paris]");

        ProviderTransactionDTO transaction01 = account0.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("196.33")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction01.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-11-03").atStartOfDay(zoneId)));
        assertThat(transaction01.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction01.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction01.getDescription()).isEqualTo("PRLV GMF ASSURANCES");
        ExtendedTransactionDTO extendedTransaction01 = transaction01.getExtendedTransaction();
        assertThat(extendedTransaction01.getValueDate()).isEqualTo("2020-11-03T00:00+01:00[Europe/Paris]");

        ProviderTransactionDTO transaction02 = account0.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("18.21")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction02.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-11-02").atStartOfDay(zoneId)));
        assertThat(transaction02.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction02.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction02.getDescription()).isEqualTo("ANNUL CB MGP*Vinted");
        ExtendedTransactionDTO extendedTransaction02 = transaction02.getExtendedTransaction();
        assertThat(extendedTransaction02.getValueDate()).isEqualTo("2020-11-02T00:00+01:00[Europe/Paris]");

        ProviderTransactionDTO transaction03 = account0.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("54.42")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction03.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-10-29").atStartOfDay(zoneId)));
        assertThat(transaction03.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction03.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction03.getDescription()).isEqualTo("CB CARREFOUR DAC");
        ExtendedTransactionDTO extendedTransaction03 = transaction03.getExtendedTransaction();
        assertThat(extendedTransaction03.getValueDate()).isEqualTo("2020-10-29T00:00+01:00[Europe/Paris]");

        //account1
        ProviderAccountDTO account1 = response.getAccounts().stream()
                .filter(a -> a.getAccountId().equals("CTR|1271339|10987654321|1|2|1|10987654321"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No account found"));
        assertThat(account1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account1.getAvailableBalance()).isEqualTo("1200.38");
        assertThat(account1.getCurrentBalance()).isEqualTo("1200.38");
        assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account1.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account1.getAccountNumber().getIdentification()).isEqualTo("FR7612548029981098765432122");
        assertThat(account1.getTransactions()).hasSize(4);
        ProviderTransactionDTO transaction10 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("21.49")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction10.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-11-02").atStartOfDay(zoneId)));
        assertThat(transaction10.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction10.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction10.getDescription()).isEqualTo("CB MGP*Vinted 11254");
        ExtendedTransactionDTO extendedTransaction10 = transaction10.getExtendedTransaction();
        assertThat(extendedTransaction10.getValueDate()).isEqualTo("2020-11-02T00:00+01:00[Europe/Paris]");

        ProviderTransactionDTO transaction11 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("27.4")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction11.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-11-02").atStartOfDay(zoneId)));
        assertThat(transaction11.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction11.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction11.getDescription()).isEqualTo("CB ASF VIENNE");
        ExtendedTransactionDTO extendedTransaction11 = transaction11.getExtendedTransaction();
        assertThat(extendedTransaction11.getValueDate()).isEqualTo("2020-11-02T00:00+01:00[Europe/Paris]");

        ProviderTransactionDTO transaction12 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("89.25")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction12.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-11-02").atStartOfDay(zoneId)));
        assertThat(transaction12.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction12.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction12.getDescription()).isEqualTo("CB MONOPRIX");
        ExtendedTransactionDTO extendedTransaction12 = transaction12.getExtendedTransaction();
        assertThat(extendedTransaction12.getValueDate()).isEqualTo("2020-11-02T00:00+01:00[Europe/Paris]");

        ProviderTransactionDTO transaction13 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("20.48")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction13.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-11-02").atStartOfDay(zoneId)));
        assertThat(transaction13.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction13.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction13.getDescription()).isEqualTo("ANNUL CB MGP*Vinted");
        ExtendedTransactionDTO extendedTransaction13 = transaction13.getExtendedTransaction();
        assertThat(extendedTransaction13.getValueDate()).isEqualTo("2020-11-02T00:00+01:00[Europe/Paris]");
    }
}

