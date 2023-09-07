package com.yolt.providers.openbanking.ais.sainsburys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
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
 * This test contains case according to documentation, when request acquiring accounts fail due to 400.
 * This means that there was a mistake in our request, thus we can't map such account
 * (so throw {@link ProviderFetchDataException})
 */
@SpringBootTest(classes = {SainsburysApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sainsburys/ais-3.1.1/accounts-400", httpsPort = 0, port = 0)
@ActiveProfiles("sainsburys")
public class SainsburysDataProviderV2FetchData400IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final Signer SIGNER = new SignerMock();

    private static RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("SainsburysDataProviderV2")
    private GenericBaseDataProvider dataProvider;

    @Autowired
    @Qualifier("SainsburysObjectMapper")
    private ObjectMapper objectMapper;

    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = SainsburysSampleTypedAuthMeansV2.getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> UUID.randomUUID().toString());
    }

    @Test
    public void shouldThrowProviderFetchDataExceptionWhenResponseStatusIs400() throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchData);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(ProviderFetchDataException.class);
    }
}
