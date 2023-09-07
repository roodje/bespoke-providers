package com.yolt.providers.redsys.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV2;
import com.yolt.providers.redsys.common.util.RedsysPKCE;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;

import java.time.Clock;

public abstract class RedsysDataProviderV2 extends RedsysDataGenericProvider {

    public RedsysDataProviderV2(final RedsysBaseProperties properties,
                                final RedsysAuthorizationService authorizationService,
                                final RedsysFetchDataServiceV2 fetchDataService,
                                final ObjectMapper mapper,
                                final Clock clock) {
        super(properties, authorizationService, fetchDataService, mapper, clock);
    }

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        RedsysAuthenticationMeans authenticationMeans = RedsysAuthenticationMeans.fromAuthenticationMeans(
                urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());

        final OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange = RedsysPKCE.createRandomS256();

        String authorizationUrl = createAuthorizationUrl(authenticationMeans.getClientId(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState(),
                oAuth2ProofKeyCodeExchange);

        String providerState = serializeAccessMeans(new RedsysAccessMeans(oAuth2ProofKeyCodeExchange.getCodeVerifier()));

        return new RedirectStep(authorizationUrl, null, providerState);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return createActualAccessMeans(urlCreateAccessMeans, true);
    }
}
