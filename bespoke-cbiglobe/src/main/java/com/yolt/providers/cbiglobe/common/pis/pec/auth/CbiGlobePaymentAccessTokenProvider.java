package com.yolt.providers.cbiglobe.common.pis.pec.auth;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClientFactory;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

@RequiredArgsConstructor
public class CbiGlobePaymentAccessTokenProvider {

    private final CbiGlobePisHttpClientFactory httpClientFactory;
    private final CbiGlobePisAuthenticationService authenticationService;
    private final ProviderIdentification providerIdentification;

    public Token provideClientAccessToken(RestTemplateManager restTemplateManager,
                                          CbiGlobeAuthenticationMeans authMeans,
                                          AuthenticationMeansReference authenticationMeansReference) {
        var httpClient = httpClientFactory.createPisHttpClient(authMeans, restTemplateManager, providerIdentification.getProviderDisplayName());
        return authenticationService.getClientAccessToken(httpClient, authMeans, authenticationMeansReference);
    }
}
