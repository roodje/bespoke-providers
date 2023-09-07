package com.yolt.providers.openbanking.ais.monzogroup.ais.V5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains flows with client mismatch occurring in Monzo bank provider.
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/ais/accounts-400-client-mismatch/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoDataProviderV5ClientMismatch400IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static AccessMeans VALID_MONZO_ACCESS_MEANS;

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("MonzoDataProviderV5")
    private GenericBaseDataProvider monzoDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() {
        VALID_MONZO_ACCESS_MEANS = new AccessMeans(
                Instant.now(),
                USER_ID,
                "Az90SAOJklae",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "93bac548-d2de-4546-b106-880a5018460d");
    }

    @Test
    public void shouldReturnCorrectlyFetchData() throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, getSerializedAccessMeans(VALID_MONZO_ACCESS_MEANS), new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowingCallable fetchDataCallable = () ->  monzoDataProvider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }

    private String getSerializedAccessMeans(AccessMeans monzoGroupAccessMeans) {
        try {
            return objectMapper.writeValueAsString(monzoGroupAccessMeans);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken", e);
        }
    }
}
