package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupApp;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.SneakyThrows;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains case according to documentation, when request to refresh token is called without refresh token value.
 * This means that this operation is not possible, thus we want to force user to fill a consent (so throw {@link TokenInvalidException})
 * <p>
 * Covered flows:
 * - refreshing access means
 * <p>
 */
@SpringBootTest(classes = {CapitalOneGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("capitalonegroup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CapitalOneDataProviderV3RefreshTokenMissingRefreshTokenIntegrationTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://test-redirect-url.com/identifier";
    private static final String TEST_ACCESS_TOKEN = "TEST_ACCESS_TOKEN";

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = CapitalOneGroupSampleAuthenticationMeans.getSampleAuthenticationMeans();
    private static final RestTemplateManagerMock REST_TEMPLATE_MANAGER_MOCK = new RestTemplateManagerMock(() -> "35acdd5c-ddf1-4a70-ac0f-a4322e3bc263");

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("CapitalOneDataProviderV4")
    private CapitalOneGroupDataProviderV3 capitalOneDataProviderV4;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(capitalOneDataProviderV4);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new CapitalOneGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowTokenInvalidExceptionWhenRefreshTokenIsNull(UrlDataProvider provider) {
        // given
        UrlRefreshAccessMeansRequest refreshRequest = createUrlRefreshAccessMeansRequestWithMissingRefreshToken();

        // when
        ThrowableAssert.ThrowingCallable refreshTokenCallable = () -> provider.refreshAccessMeans(refreshRequest);

        // then
        assertThatThrownBy(refreshTokenCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Refresh token is missing, and access token is expired.");
    }

    @SneakyThrows
    private UrlRefreshAccessMeansRequest createUrlRefreshAccessMeansRequestWithMissingRefreshToken() {
        AccessMeans oAuthToken = new AccessMeans(Instant.now(), UUID.randomUUID(), TEST_ACCESS_TOKEN, null, new Date(), new Date(), TEST_REDIRECT_URL);
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(TEST_USER_ID, serializedOAuthToken, new Date(), new Date());
        return new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();
    }
}
