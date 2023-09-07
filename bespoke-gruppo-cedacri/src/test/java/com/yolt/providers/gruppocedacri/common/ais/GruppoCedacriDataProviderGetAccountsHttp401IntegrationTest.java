package com.yolt.providers.gruppocedacri.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.gruppocedacri.FakeRestTemplateManager;
import com.yolt.providers.gruppocedacri.GruppoCedacriSampleTypedAuthenticationMeans;
import com.yolt.providers.gruppocedacri.GruppoCedacriTestApp;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriDataProviderV1;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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
 * This test suite contains case where consent call returns 401 error in Gruppo Cedacri providers.
 * Tests are parametrized and run for all {@link GruppoCedacriDataProviderV1} providers in group.
 * Covered flows:
 * - fetching accounts
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = GruppoCedacriTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/gruppocedacri/ais/get-accounts-401/", httpsPort = 0, port = 0)
@ActiveProfiles("gruppocedacri")
class GruppoCedacriDataProviderGetAccountsHttp401IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CONSENT_ID = "8c929c62-53f3-4543-97c0-0aed02b1d9bc";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "o9xcq8V2zUg893gm6ROpO7XDUhaBkIOyilSHG0M11XCXFgjMPP7U6R";

    @Autowired
    @Qualifier("BancaMediolanumDataProviderV1")
    private GruppoCedacriDataProviderV1 bancaMediolanumDataProviderV1;

    Stream<UrlDataProvider> getGruppoCedacriProviders() {
        return Stream.of(bancaMediolanumDataProviderV1);
    }

    @Autowired
    @Qualifier("GruppoCedacri")
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void beforeEach() {
        authenticationMeans = new GruppoCedacriSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldThrowTokenInvalidExceptionOnHttp401(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.fetchData(request))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(ACCESS_TOKEN);
        tokenResponse.setExpiresIn(3920);
        tokenResponse.setTokenType("Bearer");

        GruppoCedacriAccessMeans accessMeans = new GruppoCedacriAccessMeans(tokenResponse, CONSENT_ID);

        return new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(accessMeans),
                new Date(),
                Date.from(Instant.now().plusSeconds(600)));
    }
}