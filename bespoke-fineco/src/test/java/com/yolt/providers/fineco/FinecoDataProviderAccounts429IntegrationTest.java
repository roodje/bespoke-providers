package com.yolt.providers.fineco;

import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when there are too many request per day without PSU-IP-Address
 * header and 429 error code is returned. For this case we don't want to inform user about the error so
 * {@link BackPressureRequestException}) is thrown
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = FinecoTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/ais/accounts-429", httpsPort = 0, port = 0)
@ActiveProfiles("fineco")
public class FinecoDataProviderAccounts429IntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    private FinecoDataProviderV3 provider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    Stream<UrlDataProvider> finecoProviders() {
        return Stream.of(provider);
    }

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        authenticationMeans = new FinecoSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("finecoProviders")
    void shouldThrowBackPressureRequestExceptionWhenAccountsRequestFail(UrlDataProvider providerUnderTest) {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(),
                "{\"consentId\":\"consentId\"," +
                        "\"consentCreateTime\":\"1970-01-01T00:00:00.999Z\"," +
                        "\"consentExpireTime\":\"1970-01-01T00:00:01.999Z\"}",
                new Date(),
                new Date());

        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> providerUnderTest.fetchData(fetchDataRequest);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(BackPressureRequestException.class);
    }
}
