package com.yolt.providers.openbanking.ais.barclaysgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysApp;
import com.yolt.providers.openbanking.ais.barclaysgroup.BarclaysSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BarclaysApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/barclaysgroup/ais-3.1/v3/filtering-blocked-account", httpsPort = 0, port = 0)
@ActiveProfiles("barclays")
public class BarclaysDataProviderFetchDataWithFilteringAccountsIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static String SERIALIZED_ACCESS_MEANS;
    private static final String BLOCKED_ACCOUNT_ID = "20000000000001449160";

    private static final Signer SIGNER = new SignerMock();

    private RestTemplateManagerMock restTemplateManagerMock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private String requestTraceId = "d0a9b85f-9715-4d16-a33d-4323ceab5254";

    @Autowired
    @Qualifier("BarclaysDataProviderV16")
    private GenericBaseDataProviderV2 provider;

    @Autowired
    @Qualifier("BarclaysObjectMapperV2")
    private ObjectMapper objectMapper;

    @BeforeAll
    public void setup() throws JsonProcessingException {
        AccessMeansState<AccessMeans> accessMeansState = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL),
                List.of("ReadParty"));
        SERIALIZED_ACCESS_MEANS = objectMapper.writeValueAsString(accessMeansState);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new BarclaysSampleTypedAuthenticationMeans().getAuthenticationMean();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
    }

    @Test
    public void shouldSuccessfullyFetchDataWithoutBlockedAccount() throws Exception {
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts())
                .extracting(ProviderAccountDTO::getAccountId)
                .doesNotContain(BLOCKED_ACCOUNT_ID)
                .hasSize(1);


    }
}
