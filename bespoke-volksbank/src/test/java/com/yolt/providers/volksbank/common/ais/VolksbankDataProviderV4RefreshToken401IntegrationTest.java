package com.yolt.providers.volksbank.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.volksbank.FakeRestTemplateManager;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.VolksbankTestApp;
import com.yolt.providers.volksbank.common.model.VolksbankAccessMeans;
import com.yolt.providers.volksbank.common.model.VolksbankAccessTokenResponse;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test suite contains case where token endpoint return error when refreshing access token Volksbank group providers.
 * Tests are parametrized and run for all {@link VolksbankDataProviderV4} providers in group.
 * Covered flows:
 * - refreshing access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = VolksbankTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/volksbank/api_1.1/ais/refresh_token_401", port = 0, httpsPort = 0)
@ActiveProfiles("volksbank")
public class VolksbankDataProviderV4RefreshToken401IntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/";
    private static final String VOLKSBANK_CONSENT_ID = "CONSENT_ID";
    private static final String ACCESS_TOKEN = "3fb45310-e2bb-11ea-87d0-0242ac130003";
    private static final String REFRESH_TOKEN = "4a33007a-e2bb-11ea-87d0-0242ac130003";

    @Autowired
    @Qualifier("RegioDataProviderV5")
    private VolksbankDataProviderV4 regioProviderV5;

    @Autowired
    @Qualifier("SNSDataProviderV5")
    private VolksbankDataProviderV4 snsProviderV5;

    @Autowired
    @Qualifier("ASNDataProviderV5")
    private VolksbankDataProviderV4 asnProviderV5;

    Stream<UrlDataProvider> getVolksbankProviders() {
        return Stream.of(regioProviderV5, snsProviderV5, asnProviderV5);
    }

    @Autowired
    @Qualifier("Volksbank")
    private ObjectMapper mapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private List<StubMapping> stubMappings = new ArrayList<>();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @AfterEach
    public void afterEach() {
        stubMappings.forEach(WireMock::removeStub);
        stubMappings.clear();
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldThrowTokenInvalidExceptionWhenRefreshingAccessMeans(UrlDataProvider dataProviderUnderTest) throws IOException {
        //given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProviderUnderTest.refreshAccessMeans(request);
        // then
        assertThatExceptionOfType(TokenInvalidException.class).isThrownBy(throwingCallable).withMessage("Error with status code: 401 received during call");
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        VolksbankAccessTokenResponse accessTokenResponseDTO = new VolksbankAccessTokenResponse();
        accessTokenResponseDTO.setAccessToken(ACCESS_TOKEN);
        accessTokenResponseDTO.setRefreshToken(REFRESH_TOKEN);
        accessTokenResponseDTO.setTokenType("bearer");
        accessTokenResponseDTO.setExpiresIn(600);
        accessTokenResponseDTO.setScope("AIS");

        VolksbankAccessMeans accessMeansDTO = new VolksbankAccessMeans(
                accessTokenResponseDTO,
                REDIRECT_URL,
                VOLKSBANK_CONSENT_ID

        );

        return new AccessMeansDTO(
                UUID.randomUUID(),
                mapper.writeValueAsString(accessMeansDTO),
                new Date(),
                Date.from(Instant.now().plusSeconds(600)));
    }
}