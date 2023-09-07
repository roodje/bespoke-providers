package com.yolt.providers.direkt1822group.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.direkt1822group.Direkt1822GroupSampleAuthenticationMeans;
import com.yolt.providers.direkt1822group.TestApp;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This test class contains a test when the bank's API returns HTTP-403 status code on data endpoint.
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("direkt1822group")
@AutoConfigureWireMock(stubs = "classpath:/mappings/direkt1822group/ais/accounts-403", httpsPort = 0, port = 0)
class Direkt1822GroupDataProvider403IntegrationTest {

    @Autowired
    @Qualifier("Direkt1822DataProvider")
    private Direkt1822GroupDataProvider direkt1822GroupDataProvider;

    @Autowired
    @Qualifier("Direkt1822GroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    Stream<Direkt1822GroupDataProvider> direkt1822GroupDataProviders() {
        return Stream.of(direkt1822GroupDataProvider);
    }

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = Direkt1822GroupSampleAuthenticationMeans.get();
    private static final String TEST_PSU_IP_ADDRESS = "12.34.56.78";

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldThrowTokenInvalidExceptionOnHttp403(Direkt1822GroupDataProvider dataProvider) {
        //given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(getSampleAccessMeans())
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        //when
        assertThatThrownBy(() -> dataProvider.fetchData(urlFetchData))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @SneakyThrows
    private AccessMeansDTO getSampleAccessMeans() {
        return new AccessMeansDTO(
                UUID.randomUUID(),
                objectMapper.writeValueAsString(new Direkt1822GroupAccessMeans("12345")),
                new Date(),
                new Date());
    }
}