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
 * This test contains fetch data flow when errors occur on optional endpoints in Barclays provider.
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions, standing orders
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/barclaysgroup/ais-3.1/v3/error-on-optional-endpoints", httpsPort = 0, port = 0)
@ActiveProfiles("barclays")
public class BarclaysDataProviderFetchDataOptionalEndpointsErrorIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static String SERIALIZED_ACCESS_MEANS_STATE;

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
        AccessMeans token = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        AccessMeansState<AccessMeans> accessMeansState = new AccessMeansState<>(token, List.of("ReadParty"));
        SERIALIZED_ACCESS_MEANS_STATE = objectMapper.writeValueAsString(accessMeansState);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldNotThrowExceptionWhenErrorOccursOnOptionalEndpoints(UrlDataProvider subject) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDTO(SERIALIZED_ACCESS_MEANS_STATE))
                .setAuthenticationMeans(new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean())
                .setRestTemplateManager(new RestTemplateManagerMock(() -> requestTraceId))
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        assertThat(dataProviderResponse.getAccounts().get(0).getAccountNumber().getHolderName()).isNull();
        assertThat(dataProviderResponse.getAccounts().get(0).getStandingOrders()).isEmpty();
        assertThat(dataProviderResponse.getAccounts().get(0).getDirectDebits()).isEmpty();
    }

    private AccessMeansDTO createAccessMeansDTO(String accessMeans) {
        return new AccessMeansDTO(USER_ID, accessMeans, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    }
}
