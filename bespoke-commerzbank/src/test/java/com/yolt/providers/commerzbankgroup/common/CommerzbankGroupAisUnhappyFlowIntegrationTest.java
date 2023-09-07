package com.yolt.providers.commerzbankgroup.common;

import com.yolt.providers.commerzbankgroup.TestApp;
import com.yolt.providers.commerzbankgroup.TestRestTemplateManager;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unhappy-flow", httpsPort = 0, port = 0)
public class CommerzbankGroupAisUnhappyFlowIntegrationTest {

    public static final String SERIALIZED_400_ERROR_ACCESS_MEANS = "{\"accessToken\":\"400_ACCESS_TOKEN\",\"refreshToken\":\"SOME_REFRESH_TOKEN\",\"consentId\":\"1234-wertiq-983\"}";
    public static final String SERIALIZED_401_ERROR_ACCESS_MEANS = "{\"accessToken\":\"401_ACCESS_TOKEN\",\"refreshToken\":\"SOME_REFRESH_TOKEN\",\"consentId\":\"1234-wertiq-983\"}";
    public static final String SERIALIZED_403_ERROR_ACCESS_MEANS = "{\"accessToken\":\"403_ACCESS_TOKEN\",\"refreshToken\":\"SOME_REFRESH_TOKEN\",\"consentId\":\"1234-wertiq-983\"}";
    public static final String SERIALIZED_429_ERROR_ACCESS_MEANS = "{\"accessToken\":\"429_ACCESS_TOKEN\",\"refreshToken\":\"SOME_REFRESH_TOKEN\",\"consentId\":\"1234-wertiq-983\"}";

    @Autowired
    @Qualifier("CommerzbankProvider")
    private UrlDataProvider commerzbankGroupUrlDataProvider;
    @Autowired
    private Clock clock;
    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private TestRestTemplateManager restTemplateManager;

    public Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(commerzbankGroupUrlDataProvider);
    }

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = CommerzbankGroupSampleAuthenticationMeans.getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrow400ErrorProviderHttpStatusExceptionWhenAccountRequestFails(UrlDataProvider dataProvider) {

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setPsuIpAddress("192.168.16.5")
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(UUID.randomUUID(), SERIALIZED_400_ERROR_ACCESS_MEANS, Date.from(Instant.now(clock)), Date.from(Instant.now(clock)))
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchDataRequest);

        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(ProviderHttpStatusException.class);

    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrow401ErrorTokenInvalidExceptionWhenAccountRequestFails(UrlDataProvider dataProvider) {

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setPsuIpAddress("192.168.16.5")
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(UUID.randomUUID(), SERIALIZED_401_ERROR_ACCESS_MEANS, Date.from(Instant.now(clock)), Date.from(Instant.now(clock)))
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchDataRequest);

        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);

    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrow403ErrorTokenInvalidExceptionWhenAccountRequestFails(UrlDataProvider dataProvider) {

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setPsuIpAddress("192.168.16.5")
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(UUID.randomUUID(), SERIALIZED_403_ERROR_ACCESS_MEANS, Date.from(Instant.now(clock)), Date.from(Instant.now(clock)))
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchDataRequest);

        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(TokenInvalidException.class);

    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrow429ErrorBackPressureRequestExceptionWhenAccountRequestFails(UrlDataProvider dataProvider) {

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setPsuIpAddress("192.168.16.5")
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(UUID.randomUUID(), SERIALIZED_429_ERROR_ACCESS_MEANS, Date.from(Instant.now(clock)), Date.from(Instant.now(clock)))
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(urlFetchDataRequest);

        assertThatThrownBy(fetchDataCallable)
                .isExactlyInstanceOf(BackPressureRequestException.class);

    }
}
