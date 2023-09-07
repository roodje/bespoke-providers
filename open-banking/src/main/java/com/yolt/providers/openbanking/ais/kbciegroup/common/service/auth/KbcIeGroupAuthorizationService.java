package com.yolt.providers.openbanking.ais.kbciegroup.common.service.auth;

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

public class KbcIeGroupAuthorizationService extends DefaultAuthenticationService {

    private final String oauthAuthorizationUrl;

    public KbcIeGroupAuthorizationService(String oauthAuthorizationUrl, Oauth2Client oauth2Client, UserRequestTokenSigner userRequestTokenSigner, TokenClaimsProducer tokenClaimsProducer, Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
        this.oauthAuthorizationUrl = oauthAuthorizationUrl;
    }

    @Override
    public String generateAuthorizationUrl(DefaultAuthMeans authenticationMeans, String resourceId, String secretState, String redirectUrl, TokenScope scope, Signer signer) {
        try {
            String adjustedRedirectUrl = adjustRedirectUrl(redirectUrl);
            String userRequestToken = generateUserRequestToken(authenticationMeans, resourceId, secretState,
                    adjustedRedirectUrl, signer, scope);

            URIBuilder uriBuilder = new URIBuilder(oauthAuthorizationUrl)
                    .addParameter(OAuth.RESPONSE_TYPE, OAuth.CODE)
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter(OAuth.REDIRECT_URI, adjustedRedirectUrl)
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter(OAuth.REQUEST, userRequestToken);
            return uriBuilder.toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }
}
