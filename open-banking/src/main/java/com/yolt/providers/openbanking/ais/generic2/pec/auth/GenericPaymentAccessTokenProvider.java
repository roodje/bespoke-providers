package com.yolt.providers.openbanking.ais.generic2.pec.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

import java.util.UUID;

@RequiredArgsConstructor
public class GenericPaymentAccessTokenProvider {

    private final HttpClientFactory httpClientFactory;
    private final AuthenticationService authenticationService;
    private final GenericPaymentAuthorizationCodeExtractor paymentAuthorizationCodeExtractor;
    private final GenericPaymentRedirectUrlExtractor paymentRedirectUrlExtractor;
    private final ProviderIdentification providerIdentification;
    private final TokenScope scope;

    public AccessMeans provideClientAccessToken(RestTemplateManager restTemplateManager,
                                                DefaultAuthMeans authMeans,
                                                AuthenticationMeansReference authenticationMeansReference,
                                                Signer signer) {
        var httpClient = httpClientFactory.createHttpClient(restTemplateManager, authMeans, providerIdentification.getDisplayName());
        return authenticationService.getClientAccessToken(httpClient, authMeans, authenticationMeansReference, scope, signer);
    }

    public AccessMeans provideUserAccessToken(RestTemplateManager restTemplateManager,
                                              DefaultAuthMeans authMeans,
                                              String redirectUrlPostedBackFromSite,
                                              Signer signer,
                                              UUID userId) throws TokenInvalidException, ConfirmationFailedException {
        var authorizationCode = paymentAuthorizationCodeExtractor.extractAuthorizationCode(redirectUrlPostedBackFromSite);
        var redirectUrl = paymentRedirectUrlExtractor.extractPureRedirectUrl(redirectUrlPostedBackFromSite);
        var httpClient = httpClientFactory.createHttpClient(restTemplateManager, authMeans, providerIdentification.getDisplayName());
        return authenticationService.createAccessToken(httpClient, authMeans, userId, authorizationCode, redirectUrl, scope, signer);
    }
}
