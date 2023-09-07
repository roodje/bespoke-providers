package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.util.UUID;


public class BankOfIrelandGroupAuthenticationService extends DefaultAuthenticationService {

    public BankOfIrelandGroupAuthenticationService(String oauthAuthorizationUrl,
                                                   Oauth2Client oauth2Client,
                                                   UserRequestTokenSigner userRequestTokenSigner,
                                                   TokenClaimsProducer tokenClaimsProducer,
                                                   Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
    }

    @Override
    public AccessMeans refreshAccessToken(HttpClient httpClient,
                                          DefaultAuthMeans authenticationMeans,
                                          UUID userId,
                                          String refreshToken,
                                          String redirectUrl,
                                          TokenScope scope,
                                          Signer signer) throws TokenInvalidException {
        AccessMeans accessMeans = super.refreshAccessToken(httpClient, authenticationMeans, userId, refreshToken, redirectUrl, scope, signer);
        if (StringUtils.isEmpty(accessMeans.getRefreshToken())) {
            accessMeans.setRefreshToken(refreshToken);
        }
        return accessMeans;
    }
}
