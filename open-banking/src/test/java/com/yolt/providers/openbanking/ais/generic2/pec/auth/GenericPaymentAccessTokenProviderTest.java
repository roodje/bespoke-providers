package com.yolt.providers.openbanking.ais.generic2.pec.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericPaymentAccessTokenProviderTest {

    private GenericPaymentAccessTokenProvider subject;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private GenericPaymentAuthorizationCodeExtractor authorizationCodeExtractor;

    @Mock
    private GenericPaymentRedirectUrlExtractor redirectUrlExtractor;

    @Mock
    private TokenScope tokenScope;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private Signer signer;

    @Mock
    private HttpClient httpClient;

    @BeforeEach
    void beforeEach() {
        subject = new GenericPaymentAccessTokenProvider(httpClientFactory,
                authenticationService,
                authorizationCodeExtractor,
                redirectUrlExtractor,
                new ProviderIdentification("PROVIDER", "Provider", ProviderVersion.VERSION_1),
                tokenScope);
    }

    @Test
    void shouldReturnAccessMeansWithClientAccessTokenWhenCorrectDataAreProvided() {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        AuthenticationMeansReference authenticationMeansReference = prepareAuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
        AccessMeans accessMeans = prepareAccessMeans();

        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString()))
                .willReturn(httpClient);
        given(authenticationService.getClientAccessToken(any(HttpClient.class), any(DefaultAuthMeans.class), any(AuthenticationMeansReference.class), any(TokenScope.class), any(Signer.class)))
                .willReturn(accessMeans);

        // when
        AccessMeans result = subject.provideClientAccessToken(restTemplateManager, authMeans, authenticationMeansReference, signer);

        // then
        then(httpClientFactory)
                .should()
                .createHttpClient(restTemplateManager, authMeans, "Provider");
        then(authenticationService)
                .should()
                .getClientAccessToken(httpClient, authMeans, authenticationMeansReference, tokenScope, signer);
        assertThat(result).isEqualTo(accessMeans);
    }

    @Test
    void shouldReturnAccessMeansWithUserAccessTokenWhenCorrectDataAreProvided() throws TokenInvalidException, ConfirmationFailedException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        AccessMeans accessMeans = prepareAccessMeans();
        String redirectUrlPostedBackFromSite = "redirectUrlPostedBackFromSite";
        UUID userId = UUID.randomUUID();

        given(authorizationCodeExtractor.extractAuthorizationCode(anyString()))
                .willReturn("authorizationCode");
        given(redirectUrlExtractor.extractPureRedirectUrl(anyString()))
                .willReturn("pureRedirectUrl");
        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString()))
                .willReturn(httpClient);
        given(authenticationService.createAccessToken(any(HttpClient.class), any(DefaultAuthMeans.class), any(UUID.class), anyString(), anyString(), any(TokenScope.class), any(Signer.class)))
                .willReturn(accessMeans);

        // when
        AccessMeans result = subject.provideUserAccessToken(restTemplateManager, authMeans, redirectUrlPostedBackFromSite, signer, userId);

        // then
        then(authorizationCodeExtractor)
                .should()
                .extractAuthorizationCode("redirectUrlPostedBackFromSite");
        then(redirectUrlExtractor)
                .should()
                .extractPureRedirectUrl("redirectUrlPostedBackFromSite");
        then(httpClientFactory)
                .should()
                .createHttpClient(restTemplateManager, authMeans, "Provider");
        then(authenticationService)
                .should()
                .createAccessToken(httpClient, authMeans, userId, "authorizationCode", "pureRedirectUrl", tokenScope, signer);

        assertThat(result).isEqualTo(accessMeans);
    }

    private AccessMeans prepareAccessMeans() {
        return new AccessMeans(null,
                null,
                "accessToken",
                null,
                null,
                null,
                null);
    }

    private AuthenticationMeansReference prepareAuthenticationMeansReference(UUID clientId, UUID redirectUrlId) {
        return new AuthenticationMeansReference(clientId, redirectUrlId);
    }
}