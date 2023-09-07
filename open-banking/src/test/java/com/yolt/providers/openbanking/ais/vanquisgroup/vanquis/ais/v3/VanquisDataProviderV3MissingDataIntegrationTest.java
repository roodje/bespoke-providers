package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupApp;
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisGroupSampleTypedAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.VanquisGroupBaseDataProviderV2;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
 * This test contains missing data flow occurring in Vanquis bank provider.
 * <p>
 * Disclaimer: Vanquis is a single bank, so there is no need to parametrize this test class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {VanquisGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/vanquisgroup/ais/ob_3.1.1/missing-data", httpsPort = 0, port = 0)
@ActiveProfiles("vanquisgroupV1")
public class VanquisDataProviderV3MissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final SignerMock SIGNER = new SignerMock();
    private static String SERIALIZED_ACCESS_MEANS;

    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    @Qualifier("VanquisDataProviderV3")
    private VanquisGroupBaseDataProviderV2 vanquisDataProvider;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        Instant now = Instant.now();
        AccessMeans token = new AccessMeans(
                now,
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(now.plus(1, ChronoUnit.DAYS)),
                Date.from(now),
                TEST_REDIRECT_URL);
        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        authenticationMeans = new VanquisGroupSampleTypedAuthenticationMeansV2().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnCorrectlyFetchDataWhenThereIsMissingDataInBankResponse(VanquisGroupBaseDataProviderV2 dataProvider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        assertThat(account.getAvailableBalance()).isNull();
        assertThat(account.getCurrentBalance()).isEqualTo("1.00");
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getName()).isEqualTo("Vanquis Bank Account");
        assertThat(account.getExtendedAccount().getCurrency()).isNull();
        assertThat(account.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(account.getTransactions()).hasSize(3);
        ProviderTransactionDTO transaction = account.getTransactions().get(0);
        assertThat(transaction.getAmount()).isNull();
        assertThat(transaction.getStatus()).isNull();
        assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(transaction.getExtendedTransaction().getTransactionAmount().getAmount()).isNull();
        transaction = account.getTransactions().get(1);
        assertThat(transaction.getAmount()).isEqualTo("0.65");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction.getType()).isNull();
        assertThat(transaction.getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        assertThat(transaction.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-0.65");
        transaction = account.getTransactions().get(2);
        assertThat(transaction.getAmount()).isEqualTo("0.02");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(transaction.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("0.02");
    }

    private Stream<Provider> getDataProviders() {
        return Stream.of(vanquisDataProvider);
    }

}
