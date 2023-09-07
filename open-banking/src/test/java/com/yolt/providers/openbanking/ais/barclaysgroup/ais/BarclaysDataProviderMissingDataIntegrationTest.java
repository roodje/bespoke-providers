package com.yolt.providers.openbanking.ais.barclaysgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains tests for missing data during fetch data step in Barclays.
 * <p>
 * Covered flows:
 * - fetching data when some fields are missing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/barclaysgroup/ais-3.1/v3/missing-data/", httpsPort = 0, port = 0)
@ActiveProfiles("barclays")
public class BarclaysDataProviderMissingDataIntegrationTest {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/London");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static String SERIALIZED_ACCESS_MEANS;

    private static final Signer SIGNER = new SignerMock();

    private String requestTraceId = "d0a9b85f-9715-4d16-a33d-4323ceab5254";

    @Autowired
    @Qualifier("BarclaysDataProviderV16")
    private GenericBaseDataProviderV2 barclaysDataProviderV16;

    @Autowired
    @Qualifier("BarclaysObjectMapperV2")
    private ObjectMapper objectMapper;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(barclaysDataProviderV16);
    }

    @BeforeAll
    public void setup() throws JsonProcessingException {
        AccessMeansState<AccessMeans> token = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
        SERIALIZED_ACCESS_MEANS = objectMapper.writeValueAsString(token);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldCorrectlyFetchDataWhenSomeDataIsMissingInBanksResponse(UrlDataProvider subject) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> requestTraceId))
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getCurrentBalance()).isEqualTo("13.36");
        assertThat(account.getAvailableBalance()).isEqualTo("13.36");
        assertThat(account.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(account.getExtendedAccount().getBalances().get(0).getBalanceAmount().getCurrency()).isNull();
        assertThat(account.getExtendedAccount().getBalances().get(0).getBalanceAmount().getAmount()).isEqualTo("13.36");
        assertThat(account.getTransactions()).hasSize(3);
        ProviderTransactionDTO transaction0 = getTransactionWithSpecificOrNullAmount(account.getTransactions(), null);
        assertThat(transaction0.getAmount()).isNull();
        assertThat(transaction0.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction0.getType()).isNull();
        assertThat(transaction0.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(transaction0.getExtendedTransaction().getTransactionAmount().getAmount()).isNull();
        ProviderTransactionDTO transaction1 = getTransactionWithSpecificOrNullAmount(account.getTransactions(), new BigDecimal("6.00"));
        assertThat(transaction1.getAmount()).isEqualTo("6.00");
        assertThat(transaction1.getStatus()).isNull();
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        assertThat(transaction1.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-6.00");
        ProviderTransactionDTO transaction2 = getTransactionWithSpecificOrNullAmount(account.getTransactions(), new BigDecimal("5.00"));
        assertThat(transaction2.getAmount()).isEqualTo("5.00");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(transaction2.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-5.00");
        ZonedDateTime dateTime = OffsetDateTime.parse("2018-05-14T11:57:36+01:00", DATE_TIME_FORMATTER).toZonedDateTime().withZoneSameInstant(DEFAULT_ZONE);
        assertThat(transaction2.getDateTime()).isEqualTo(dateTime);
        assertThat(transaction2.getExtendedTransaction().getValueDate()).isEqualTo(dateTime);
        assertThat(transaction2.getExtendedTransaction().getBookingDate()).isNull();

    }

    private ProviderTransactionDTO getTransactionWithSpecificOrNullAmount(List<ProviderTransactionDTO> listOfTransactions, BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return listOfTransactions.stream()
                    .filter(transaction -> ObjectUtils.isEmpty(transaction.getAmount()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("There is no transaction with null amount in list"));
        }
        return listOfTransactions.stream()
                .filter(transaction -> amount.equals(transaction.getAmount()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("There is no transaction with amount %d", amount)));
    }
}
