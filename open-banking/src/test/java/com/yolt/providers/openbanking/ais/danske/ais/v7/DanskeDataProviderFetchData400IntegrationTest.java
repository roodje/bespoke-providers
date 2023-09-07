package com.yolt.providers.openbanking.ais.danske.ais.v7;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.openbanking.ais.TestConfiguration;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.danske.DanskeApp;
import com.yolt.providers.openbanking.ais.danske.DanskeBankDataProviderV7;
import com.yolt.providers.openbanking.ais.danske.DanskeBankSampleTypedAuthenticationMeansV7;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * This test contains case according to documentation, when request acquiring accounts fail due to 400.
 * This means that there was a mistake in our request, thus we can't map such account (so throw {@link ProviderFetchDataException})
 * TODO: The associated stub file doesn't contain RDD based HTTP 400 response body. The stub content should be updated when such RDD data found.
 */
@SpringBootTest(classes = {DanskeApp.class, TestConfiguration.class, OpenbankingConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("danske")
@AutoConfigureWireMock(stubs = "classpath:/stubs/danske/ais-3.1.6/accounts-400", httpsPort = 0, port = 0)
public class DanskeDataProviderFetchData400IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    private DanskeBankDataProviderV7 danskeBankDataProviderV7;

    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws Exception {
        authenticationMeans = new DanskeBankSampleTypedAuthenticationMeansV7().getAuthenticationMeans();
        String requestTraceId = UUID.randomUUID().toString();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        signer = new SignerMock();
    }

    @Test
    public void shouldThrowProviderFetchDataException() throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest("AccessToken");

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> danskeBankDataProviderV7.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderFetchDataException.class);
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(String accessToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build();
        AccessMeans token = new AccessMeans(USER_ID, accessToken, "", Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), null, null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }
}
