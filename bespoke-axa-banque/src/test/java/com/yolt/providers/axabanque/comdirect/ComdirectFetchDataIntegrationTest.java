package com.yolt.providers.axabanque.comdirect;

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
@AutoConfigureWireMock(stubs = "classpath:/mappings/v1/fetchdata/comdirect/happyflow", httpsPort = 0, port = 0)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComdirectFetchDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TRANSPORT_KEY_ID_VALUE = UUID.randomUUID().toString();
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = AuthMeansFixture.getAuthMeansMap(TRANSPORT_KEY_ID_VALUE);
    private static final String PST_IP_ADDRESS = "127.0.1.2";
    private static final ZoneId zoneId = ZoneId.of("Europe/Berlin");

    @Value("${wiremock.server.port}")
    private int port;

    @Autowired
    @Qualifier("ComdirectDataProviderV1")
    private UrlDataProvider comdirectDataProviderV1;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(comdirectDataProviderV1);
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
                .filter(a -> a.getAccountId().equals("8EAAC7F4F3E75F786FB71B7C111111"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No account found"));
        assertThat(account0.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(account0.getAvailableBalance()).isEqualTo("500.0");
        assertThat(account0.getCurrentBalance()).isEqualTo("500.0");
        assertThat(account0.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account0.getCreditCardData().getAvailableCreditAmount()).isEqualTo("500.0");
        assertThat(account0.getTransactions()).hasSize(2);
        ProviderTransactionDTO transaction00 = account0.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("0.94")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction00.getExternalId()).isEqualTo("910886EF984545ECA94E0A9BE8F64267");
        assertThat(transaction00.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-10-13").atStartOfDay(zoneId)));
        assertThat(transaction00.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction00.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction00.getDescription()).isEqualTo("1.75 PROZ.AUSLANDSENTGELT");
        ProviderTransactionDTO transaction01 = account0.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("195.37")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction01.getExternalId()).isEqualTo("83117CE92E654B7F8712A2A58FAA7869");
        assertThat(transaction01.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-10-30").atStartOfDay(zoneId)));
        assertThat(transaction01.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction01.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction01.getDescription()).isEqualTo("SUMME MONATSABRECHNUNG VISA");
        //account1
        ProviderAccountDTO account1 = response.getAccounts().stream()
                .filter(a -> a.getAccountId().equals("CB0160780E04423D84BAD51111111"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No account found"));
        assertThat(account1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account1.getAvailableBalance()).isEqualTo("602.06");
        assertThat(account1.getCurrentBalance()).isEqualTo("602.06");
        assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account1.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account1.getAccountNumber().getIdentification()).isEqualTo("DE352004115502222222");
        assertThat(account1.getTransactions()).hasSize(4);
        ProviderTransactionDTO transaction10 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("6.01")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction10.getExternalId()).isEqualTo("16FD1325308948C394695003518F6A8F");
        assertThat(transaction10.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-12-07").atStartOfDay(zoneId)));
        assertThat(transaction10.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction10.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction10.getDescription()).isEqualTo("PP.6822.PP . CREATELYCOM, Ihr Einka; uf bei CREATELYCOM");
        ProviderTransactionDTO transaction11 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("0.84")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction11.getExternalId()).isEqualTo("8F24CE774BF14193942AF944653200A2");
        assertThat(transaction11.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-12-07").atStartOfDay(zoneId)));
        assertThat(transaction11.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction11.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction11.getDescription()).isEqualTo("PP.6822.PP . ITUNESAPPST, Ihr Einka; uf bei ITUNESAPPST");
        ProviderTransactionDTO transaction12 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("7.24")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction12.getExternalId()).isEqualTo("BD9130CD71BD46F4B7B2225824C5C843");
        assertThat(transaction12.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-12-04").atStartOfDay(zoneId)));
        assertThat(transaction12.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction12.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction12.getDescription()).isEqualTo("EDEKA im Bahnhof//BERLIN/DE; 2020-12-03T10:42:59 KFN 1 VJ 2312");
        ProviderTransactionDTO transaction13 = account1.getTransactions().stream()
                .filter(t -> t.getAmount().equals(new BigDecimal("60.01")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No transaction found"));
        assertThat(transaction13.getExternalId()).isEqualTo("0962E375BD3546A0AAFFFDDF482DB4FD");
        assertThat(transaction13.getDateTime()).isEqualTo(ZonedDateTime.from(LocalDate.parse("2020-12-04").atStartOfDay(zoneId)));
        assertThat(transaction13.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction13.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction13.getDescription()).isEqualTo("Bargeldauszahlung; Commerzbank 00210448/Panoramastr./B; 2020-12-03T10:17:58 KFN 1 VJ 2312");
    }
}

