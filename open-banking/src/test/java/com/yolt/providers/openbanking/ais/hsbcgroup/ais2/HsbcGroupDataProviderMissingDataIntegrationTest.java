package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.HsbcGroupBaseDataProviderV7;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains tests for missing data during fetch data step in all HSBC group banks.
 * <p>
 * Disclaimer: as all providers in HSBC group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all payment providers from HSBC group
 * <p>
 * Covered flows:
 * - fetching data when some fields are missing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/hsbcgroup/ais-3.1.6"}, httpsPort = 0, port = 0)
class HsbcGroupDataProviderMissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();

    private static final String ACCESS_TOKEN = "1mH7BsLRbRcGprPlzOUbdaMYiWP";
    private static final String REFRESH_TOKEN = "OGQ1XvTAY8654BAa35umYq6yTi0";

    private static final Instant FETCH_FROM = Instant.parse("2015-01-01T00:00:00Z");

    private static final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;
    private static final SignerMock SIGNER = new SignerMock();

    private RestTemplateManager restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private HsbcGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new HsbcGroupSampleAuthenticationMeansV2();

    private String requestTraceId;

    @Autowired
    @Qualifier("HsbcDataProviderV13")
    private HsbcGroupBaseDataProviderV7 hsbcDataProviderV13;

    @Autowired
    @Qualifier("MarksAndSpencerDataProviderV13")
    private HsbcGroupBaseDataProviderV7 marksAndSpencerDataProviderV13;

    @Autowired
    @Qualifier("FirstDirectDataProviderV13")
    private HsbcGroupBaseDataProviderV7 firstDirectDataProviderV13;

    @Autowired
    @Qualifier("HsbcCorpoDataProviderV11")
    private HsbcGroupBaseDataProviderV7 hsbcCorpoDataProviderV11;


    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                hsbcDataProviderV13,
                firstDirectDataProviderV13,
                hsbcCorpoDataProviderV11,
                marksAndSpencerDataProviderV13);
    }

    @BeforeEach
    void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "d10f24f4-032a-4843-bfc9-22b599c7ae2d";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = sampleAuthenticationMeans.getHsbcGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCorrectlyFetchDataWhenSomeDataAreMissingInBankResponse(UrlDataProvider provider) throws Exception {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken = createToken(ACCESS_TOKEN, REFRESH_TOKEN);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(oAuthToken));

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        assertThat(account.getName()).contains(" Account");
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getCurrentBalance()).isNull();
        assertThat(account.getAvailableBalance()).isEqualTo("9.00");
        assertThat(account.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(account.getTransactions()).hasSize(3);
        assertThat(account.getTransactions().get(0).getAmount()).isNull();
        assertThat(account.getTransactions().get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(0).getType()).isNull();
        assertThat(account.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getAmount()).isNull();
        assertThat(account.getTransactions().get(1).getAmount()).isEqualTo("0.01");
        assertThat(account.getTransactions().get(1).getStatus()).isNull();
        assertThat(account.getTransactions().get(1).getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(account.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        assertThat(account.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("0.01");
        assertThat(account.getTransactions().get(2).getAmount()).isEqualTo("0.02");
        assertThat(account.getTransactions().get(2).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(2).getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(account.getTransactions().get(2).getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getTransactions().get(2).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("0.02");
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(final AccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(FETCH_FROM)
                .setAccessMeans(accessMeansDTO)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();
    }

    private AccessMeansDTO createAccessMeansDTO(final AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken) throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                serializeToken(oAuthToken),
                new Date(),
                Date.from(Instant.now().plus(1, DAYS)));
    }

    private String serializeToken(final AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(oAuthToken);
    }

    private AccessMeansState<HsbcGroupAccessMeansV2> createToken(final String accessToken, final String refreshToken) {
        return new AccessMeansState<>(new HsbcGroupAccessMeansV2(
                Instant.now(),
                USER_ID,
                accessToken,
                refreshToken,
                Date.from(Instant.now().plus(1, DAYS)),
                Date.from(Instant.now()),
                "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
    }
}
