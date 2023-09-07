package com.yolt.providers.openbanking.ais.generic2.service;

import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;

import java.time.Clock;

public class NoCacheAuthenticationService extends DefaultAuthenticationService {

    public NoCacheAuthenticationService(String oauthAuthorizationUrl,
                                        Oauth2Client oauth2Client,
                                        UserRequestTokenSigner userRequestTokenSigner,
                                        TokenClaimsProducer tokenClaimsProducer,
                                        Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
    }

    @Override
    protected boolean clientAccessTokenIsEmptyOrExpired(DefaultAuthenticationService.TokenKey openBankingTokenKey) {
        //Always get new access token
        return true;
    }
}
