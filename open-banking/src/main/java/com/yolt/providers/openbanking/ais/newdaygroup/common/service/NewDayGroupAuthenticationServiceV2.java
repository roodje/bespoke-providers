package com.yolt.providers.openbanking.ais.newdaygroup.common.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import org.apache.http.client.utils.URIBuilder;

import java.time.Clock;

public class NewDayGroupAuthenticationServiceV2 extends DefaultAuthenticationService {

    private final String oauthAuthorizationUrl;

    public NewDayGroupAuthenticationServiceV2(final String oauthAuthorizationUrl,
                                              final Oauth2Client oauth2Client,
                                              final UserRequestTokenSigner userRequestTokenSigner,
                                              final TokenClaimsProducer tokenClaimsProducer,
                                              final Clock clock) {
        super(oauthAuthorizationUrl,
                oauth2Client,
                userRequestTokenSigner,
                tokenClaimsProducer,
                clock);

        this.oauthAuthorizationUrl = oauthAuthorizationUrl;
    }

    @Override
    public String generateAuthorizationUrl(final DefaultAuthMeans authenticationMeans,
                                           final String resourceId,
                                           final String secretState,
                                           final String redirectUrl,
                                           final TokenScope scope,
                                           final Signer signer) {
        try {
            String userRequestToken = generateUserRequestToken(authenticationMeans, resourceId, secretState,
                    redirectUrl, signer, scope);

            URIBuilder uriBuilder = new URIBuilder(oauthAuthorizationUrl)
                    .addParameter("consentid", resourceId)
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter(OAuth.REDIRECT_URI, redirectUrl)
                    .addParameter(OAuth.RESPONSE_TYPE, "code id_token")
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter(OAuth.NONCE, secretState)
                    .addParameter(OAuth.REQUEST, userRequestToken);
            return uriBuilder.toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }
}
