package com.yolt.providers.openbanking.ais.sainsburys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
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
 * This test contains case when refresh token request contains refresh token equal to Null.
 * This means that new access token can't be created, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Covered flows:
 * - refreshing access means
 * <p>
 */
@SpringBootTest(classes = {SainsburysApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sainsburys/ais-3.1.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("sainsburys")
public class SainsburysDataProviderV2RefreshAccessMeansRefreshTokenIsNullIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final Signer SIGNER = new SignerMock();

    @Autowired
    @Qualifier("SainsburysObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("SainsburysDataProviderV2")
    private GenericBaseDataProvider dataProvider;

    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = SainsburysSampleTypedAuthMeansV2.getAuthenticationMeans();
    }

    @Test
    public void shouldThrowTokenInvalidExceptionWhenRefreshTokenIsNull() throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                USER_ID,
                "accessToken",
                null,
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        String serializedOAuthToken = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }
}
