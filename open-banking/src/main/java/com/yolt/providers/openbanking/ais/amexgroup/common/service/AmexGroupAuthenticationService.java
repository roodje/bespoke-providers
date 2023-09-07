package com.yolt.providers.openbanking.ais.amexgroup.common.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.amexgroup.common.oauth2.AmexOauthClient;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import org.apache.http.client.utils.URIBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class AmexGroupAuthenticationService extends DefaultAuthenticationService {

    private final String oauthAuthorizationUrl;
    private final Clock clock;
    private AmexOauthClient oauth2Client;

    public AmexGroupAuthenticationService(String oauthAuthorizationUrl,
                                          AmexOauthClient oauth2Client,
                                          UserRequestTokenSigner userRequestTokenSigner,
                                          TokenClaimsProducer tokenClaimsProducer,
                                          Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
        this.oauthAuthorizationUrl = oauthAuthorizationUrl;
        this.clock = clock;
        this.oauth2Client = oauth2Client;
    }

    public String generateAuthorizationUrl(DefaultAuthMeans authenticationMeans,
                                           String resourceId,
                                           String secretState,
                                           String redirectUrl,
                                           TokenScope scope,
                                           String codeChallenge,
                                           String codeChallengeMethod) {
        try {

            URIBuilder uriBuilder = new URIBuilder(oauthAuthorizationUrl)
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.REDIRECT_URI, redirectUrl)
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter("code_challenge", codeChallenge)
                    .addParameter("code_challenge_method", codeChallengeMethod)
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter("ConsentId", resourceId);
            return uriBuilder.toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    public AccessMeans createAccessToken(HttpClient httpClient,
                                         DefaultAuthMeans authenticationMeans,
                                         UUID userId,
                                         String authorizationCode,
                                         String redirectUrl,
                                         TokenScope scope,
                                         String codeVerifier) throws TokenInvalidException {
        Instant createdDate = Instant.now(clock);
        AccessTokenResponseDTO accessToken = oauth2Client.createAccessToken(httpClient, authenticationMeans,
                authorizationCode, redirectUrl, scope, codeVerifier);
        return convertToOAuthToken(userId, accessToken, redirectUrl, createdDate);
    }
}
