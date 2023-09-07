package com.yolt.providers.openbanking.ais.sainsburys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
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
 * This test contains case when refresh token request doesn't contain correct refresh token.
 * This means that new access token can't be created, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Covered flows:
 * - refreshing access means
 * <p>
 */
@SpringBootTest(classes = {SainsburysApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sainsburys/ais-3.1.1/refresh-token-400", httpsPort = 0, port = 0)
@ActiveProfiles("sainsburys")
public class SainsburysDataProviderV2RefreshAccessMeansExpiredRefreshTokenIntegrationTest {

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
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff6795");
    }

    @Test
    public void shouldThrowTokenInvalidExceptionWhenRefreshTokenIsExpired() throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        String serializedToken = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(), serializedToken, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }
}
