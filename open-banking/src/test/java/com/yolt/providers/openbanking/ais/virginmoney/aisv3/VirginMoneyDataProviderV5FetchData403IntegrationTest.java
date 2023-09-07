package com.yolt.providers.openbanking.ais.virginmoney.aisv3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyApp;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyDataProviderV5;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyJwsSigningResult;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneySampleAuthenticationMeansV3;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test suite contains case where 403 error is returned from data endpoint in Virgin Money provider.
 * This could happen when The operation was refused access. Re-authenticating the PSU may result in an appropriate token
 * that can be used. Re-authenticating the PSU (so throw {@link TokenInvalidException})
 * Covered flows:
 * - fetching accounts
 * <p>
 */
@SpringBootTest(classes = {VirginMoneyApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney")
@AutoConfigureWireMock(stubs = "classpath:/stubs/virginmoney/ais/v3/error_403", httpsPort = 0, port = 0)
public class VirginMoneyDataProviderV5FetchData403IntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";

    private RestTemplateManager restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    @Qualifier("VirginMoneyDataProviderV5")
    private VirginMoneyDataProviderV5 dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private VirginMoneySampleAuthenticationMeansV3 sampleAuthenticationMeans = new VirginMoneySampleAuthenticationMeansV3();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "12345";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = sampleAuthenticationMeans.getVirginMoneySampleAuthenticationMeansForAis();

        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new VirginMoneyJwsSigningResult());
    }

    @Test
    public void shouldThrowTokenInvalidExceptionWhenResponseStatusIs403() throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest();

        // when -> then
        assertThatThrownBy(() -> dataProvider.fetchData(urlFetchData))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    private UrlFetchDataRequest createUrlFetchDataRequest() throws JsonProcessingException {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build();
        AccessMeans token = new AccessMeans(Instant.now(), USER_ID, "accessToken", "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), Date.from(Instant.now()),
                TEST_REDIRECT_URL);
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
