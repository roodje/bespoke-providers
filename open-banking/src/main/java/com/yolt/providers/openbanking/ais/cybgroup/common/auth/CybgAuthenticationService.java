package com.yolt.providers.openbanking.ais.cybgroup.common.auth;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.service.ais.CybgGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

public class CybgAuthenticationService extends DefaultAuthenticationService {

    private final CybgGroupFetchDataServiceV3 fetchDataService;

    public CybgAuthenticationService(final String oauthAuthorizationUrl,
                                     final Oauth2Client oauth2Client,
                                     final UserRequestTokenSigner userRequestTokenSigner,
                                     final TokenClaimsProducer tokenClaimsProducer,
                                     final CybgGroupFetchDataServiceV3 fetchDataService,
                                     final Clock clock) {
        super(oauthAuthorizationUrl,
                oauth2Client,
                userRequestTokenSigner,
                tokenClaimsProducer,
                clock);
        this.fetchDataService = fetchDataService;
    }

    @Override
    public CybgGroupAccessMeansV2 createAccessToken(HttpClient httpClient,
                                                    DefaultAuthMeans authenticationMeans,
                                                    UUID userId,
                                                    String authorizationCode,
                                                    String redirectUrl,
                                                    TokenScope scope,
                                                    Signer signer) throws TokenInvalidException {
        AccessMeans accessMeans = super.createAccessToken(httpClient,
                authenticationMeans,
                userId,
                authorizationCode,
                redirectUrl,
                scope,
                signer
        );
        List<OBAccount6> accounts = fetchDataService.getAccounts(httpClient, authenticationMeans, accessMeans);
        return new CybgGroupAccessMeansV2(accessMeans, accounts);
    }
}
