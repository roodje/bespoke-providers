package com.yolt.providers.openbanking.ais.nationwide.ais.v10;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.nationwide.NationwideApp;
import com.yolt.providers.openbanking.ais.nationwide.NationwideDataProviderV10;
import com.yolt.providers.openbanking.ais.nationwide.NationwideSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains fetch data flow when errors occur on optional endpoints in Nationwide provider.
 * Covered flows:
 * - fetching accounts, balances, transactions, standing orders and parties
 */
@SpringBootTest(classes = {NationwideApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/nationwide/ais-3.1.2/optional-endpoints-error", httpsPort = 0, port = 0)
@ActiveProfiles("nationwide")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NationwideDataProviderFetchDataOptionalEndpointsErrorIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static String SERIALIZED_ACCESS_MEANS_STATE;

    @Autowired
    @Qualifier("NationwideDataProviderV11")
    private NationwideDataProviderV10 nationwideDataProviderV11;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(nationwideDataProviderV11);
    }

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        AccessMeans token = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        AccessMeansState<AccessMeans> accessMeansState = new AccessMeansState<>(token, List.of("ReadParty",
                "ReadAccountsDetail",
                "ReadBalances",
                "ReadDirectDebits",
                "ReadProducts",
                "ReadStandingOrdersDetail",
                "ReadTransactionsCredits",
                "ReadTransactionsDebits",
                "ReadTransactionsDetail"));
        SERIALIZED_ACCESS_MEANS_STATE = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(accessMeansState);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void errorOnOptionalEndpointShouldNotBrakeFlowDuringFetchDataAndDataAreProperlyMapped(UrlDataProvider provider) throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS_STATE, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(new NationwideSampleAuthenticationMeans().getAuthenticationMeans())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> "12345"))
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        assertThat(dataProviderResponse.getAccounts().get(0).getAccountNumber().getHolderName()).isNull();
        assertThat(dataProviderResponse.getAccounts().get(0).getStandingOrders()).isEmpty();
        assertThat(dataProviderResponse.getAccounts().get(0).getDirectDebits()).isEmpty();
    }
}