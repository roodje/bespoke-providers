package com.yolt.providers.rabobank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.mock.RestTemplateManagerMock;
import com.yolt.providers.mock.SignerMock;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains case according to documentation, when request acquiring accounts fail due to 500.
 * This means that request there was internal server error, thus we can't map such account (so throw {@link ProviderFetchDataException})
 *  TODO now {@link HttpServerErrorException} is thrown. It should be fixed.
 * <p>
 * Disclaimer: The group consists of only one {@link RabobankDataProvider} provider which is used for testing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/rabobank-ais/accounts-500/"}, httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
@ActiveProfiles("rabobank")
class RabobankDataProviderAccounts500IntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    private Clock clock;
    @Autowired
    @Qualifier("RabobankDataProviderV5")
    private RabobankDataProvider rabobankDataProviderV5;

    private RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock();
    private SignerMock signerMock = new SignerMock();

    @Autowired
    @Qualifier("RabobankObjectMapper")
    private ObjectMapper objectMapper;

    private RabobankSampleTypedAuthenticationMeans sampleAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    Stream<UrlDataProvider> getRabobankProviders() {
        return Stream.of(rabobankDataProviderV5);
    }

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldThrowTokenInvalidExceptionWhenAccountsRequestFail(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        String token = objectMapper.writeValueAsString(new AccessTokenResponseDTO("token",
                "refreshToken", 3600, 3600, "bearer", "AIS-Transactions-v2 AIS-Balance-v2"));

        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAccessMeans(new AccessMeansDTO(USER_ID, token, new Date(), new Date()))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.fetchData(fetchDataRequest);

        // then
        assertThatThrownBy(fetchDataCallable)
                .isInstanceOf(HttpServerErrorException.class);
    }
}
