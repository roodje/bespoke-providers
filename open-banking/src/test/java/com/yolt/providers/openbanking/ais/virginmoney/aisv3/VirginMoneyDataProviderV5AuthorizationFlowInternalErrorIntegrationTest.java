package com.yolt.providers.openbanking.ais.virginmoney.aisv3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test suite contains cases when internal errors are detected in Virgin Money provider.
 * Covered flows:
 * - refresh token is null
 * - invalid_grant error is returned with redirect url
 * <p>
 */
@SpringBootTest(classes = {VirginMoneyApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney")
public class VirginMoneyDataProviderV5AuthorizationFlowInternalErrorIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
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
    public void shouldThrowTokenInvalidExceptionWhenRefreshTokenIsNull() {
        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(createUrlRefreshAccessMeansRequest(new Jackson2ObjectMapperBuilder().build())))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowGetAccessTokenFailedExceptionWhenErrorIsInvalidGrantInQueryParameter() {
        // given
        String redirectUrl = "https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732?error=invalid_grant";
        UUID userId = UUID.randomUUID();
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl, userId);

        // when -> then
        assertThatThrownBy(() -> dataProvider.createNewAccessMeans(urlCreateAccessMeans))
                .isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }

    private UrlRefreshAccessMeansRequest createUrlRefreshAccessMeansRequest(ObjectMapper objectMapper) throws JsonProcessingException {
        AccessMeans oAuthToken = new AccessMeans(UUID.randomUUID(), "accessToken", null, new Date(), new Date(), TEST_REDIRECT_URL);
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
        return new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl, UUID userId) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }
}
