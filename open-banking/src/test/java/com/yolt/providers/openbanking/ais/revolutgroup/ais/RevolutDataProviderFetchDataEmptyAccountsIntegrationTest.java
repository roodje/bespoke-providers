package com.yolt.providers.openbanking.ais.revolutgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.openbanking.ais.TestConfiguration;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutTestApp;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * It was found out that for some users Revolut returns empty Data when fetching accounts.
 * This was a cause of  {@link NullPointerException}
 * In such scenario we would rather throw {@link ProviderFetchDataException}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {RevolutTestApp.class, TestConfiguration.class, OpenbankingConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("revolut")
@AutoConfigureWireMock(stubs = "classpath:/stubs/revolut/ais-3.1.0/accounts-empty", httpsPort = 0, port = 0)
public class RevolutDataProviderFetchDataEmptyAccountsIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("RevolutDataProviderV10")
    private GenericBaseDataProvider revolutGbDataProviderV10;


    @Autowired
    @Qualifier("RevolutEuDataProviderV8")
    private GenericBaseDataProvider revolutEuDataProviderV8;

    @Autowired
    ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(revolutEuDataProviderV8, revolutGbDataProviderV10);
    }

    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws Exception {
        authenticationMeans = new RevolutSampleAuthenticationMeans().getAuthenticationMeans();
        String requestTraceId = UUID.randomUUID().toString();
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnEmptyListOnFetchDataException(UrlDataProvider dataProvider) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest("978e46f2-b8a5-4f81-ac45-f10d32e6b764");

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderFetchDataException.class);
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(String accessToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build();
        AccessMeans token = new AccessMeans(Instant.now(), USER_ID, accessToken, "", Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), null, null);
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
