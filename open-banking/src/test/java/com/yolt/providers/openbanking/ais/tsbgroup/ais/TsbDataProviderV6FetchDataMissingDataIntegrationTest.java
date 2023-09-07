package com.yolt.providers.openbanking.ais.tsbgroup.ais;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupApp;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.tsbgroup.common.TsbGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test checks fallback mechanism when some fields values are
 * missing in bank's response.
 * <p>
 * Disclaimer: as all providers in TSB group are the same from code and stubs perspective (then only difference is configuration)
 * we are running parametrized tests for testing, but this covers all providers from TSB group.
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions, standing orders
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TsbGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tsbgroup/ais/ob_3.1.1/missing-data", httpsPort = 0, port = 0)
@ActiveProfiles("tsbgroup")
public class TsbDataProviderV6FetchDataMissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static String SERIALIZED_ACCESS_MEANS;
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("TsbDataProviderV6")
    private TsbGroupBaseDataProvider tsbDataProviderV6;
    @Mock
    private Signer signer;

    @BeforeAll
    public static void setup() throws IOException, URISyntaxException {
        AccessMeans token = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);

        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);
        authenticationMeans = new TsbGroupSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "87da2798-f7e2-4823-80c1-3c03344b8f13");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnCorrectlyFetchDataWhenSomeDataAreMissingInBankResponse(UrlDataProvider dataProvider) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        assertThat(account.getCurrentBalance()).isNull();
        assertThat(account.getAvailableBalance()).isEqualTo("1.00");
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getName()).isEqualTo("TSB Account");
        assertThat(account.getExtendedAccount().getBalances()).hasSize(3);
        BalanceDTO balance0 = account.getExtendedAccount().getBalances().stream().filter(b -> BalanceType.INTERIM_AVAILABLE.equals(b.getBalanceType())).findFirst().orElseThrow(() -> new IllegalArgumentException("Balance not found"));
        assertThat(balance0.getBalanceAmount().getCurrency()).isNull();
        assertThat(balance0.getBalanceAmount().getAmount()).isEqualTo("1.00");
        BalanceDTO balance1 = account.getExtendedAccount().getBalances().stream().filter(b -> BalanceType.PREVIOUSLY_CLOSED_BOOKED.equals(b.getBalanceType())).findFirst().orElseThrow(() -> new IllegalArgumentException("Balance not found"));
        assertThat(balance1.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(balance1.getBalanceAmount().getAmount()).isEqualTo("1.00");
        BalanceDTO balance2 = account.getExtendedAccount().getBalances().stream().filter(b -> BalanceType.CLOSING_AVAILABLE.equals(b.getBalanceType())).findFirst().orElseThrow(() -> new IllegalArgumentException("Balance not found"));
        assertThat(balance2.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(balance2.getBalanceAmount().getAmount()).isEqualTo("1.00");
        assertThat(account.getTransactions()).hasSize(3);
        assertThat(account.getTransactions().get(0).getAmount()).isNull();
        assertThat(account.getTransactions().get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(0).getType()).isNull();
        assertThat(account.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getAmount()).isNull();
        assertThat(account.getTransactions().get(1).getAmount()).isEqualTo("6.00");
        assertThat(account.getTransactions().get(1).getStatus()).isNull();
        assertThat(account.getTransactions().get(1).getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(account.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        assertThat(account.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-6.00");
        assertThat(account.getTransactions().get(2).getAmount()).isEqualTo("5.00");
        assertThat(account.getTransactions().get(2).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(2).getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(account.getTransactions().get(2).getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getTransactions().get(2).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-5.00");
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(tsbDataProviderV6);
    }
}
