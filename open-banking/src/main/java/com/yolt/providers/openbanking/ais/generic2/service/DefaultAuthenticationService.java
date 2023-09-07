package com.yolt.providers.openbanking.ais.generic2.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.exception.ClientCredentialFailedException;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.apache.http.client.utils.URIBuilder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    private final String oauthAuthorizationUrl;
    private final Oauth2Client oauth2Client;
    private final UserRequestTokenSigner userRequestTokenSigner;
    private final TokenClaimsProducer tokenClaimsProducer;
    private final Clock clock;

    private final Map<TokenKey, TokenEntry> clientAccessTokens = new ConcurrentHashMap<>();

    @Override
    public String generateAuthorizationUrl(DefaultAuthMeans authenticationMeans,
                                           String resourceId,
                                           String secretState,
                                           String redirectUrl,
                                           TokenScope scope,
                                           Signer signer) {
        try {
            String adjustedRedirectUrl = adjustRedirectUrl(redirectUrl);
            String userRequestToken = generateUserRequestToken(authenticationMeans, resourceId, secretState,
                    adjustedRedirectUrl, signer, scope);

            URIBuilder uriBuilder = new URIBuilder(oauthAuthorizationUrl)
                    .addParameter(OAuth.RESPONSE_TYPE, "code id_token")
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter(OAuth.NONCE, secretState)
                    .addParameter(OAuth.REDIRECT_URI, adjustedRedirectUrl)
                    .addParameter(OAuth.REQUEST, userRequestToken);

            return uriBuilder.toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    protected String adjustRedirectUrl(String redirectUrl) {
        return redirectUrl;
    }

    @Override
    public AccessMeans createAccessToken(HttpClient httpClient,
                                         DefaultAuthMeans authenticationMeans,
                                         UUID userId,
                                         String authorizationCode,
                                         String redirectUrl,
                                         TokenScope scope,
                                         Signer signer) throws TokenInvalidException {
        Instant createdDate = Instant.now(clock);
        AccessTokenResponseDTO accessToken = oauth2Client.createAccessToken(httpClient, authenticationMeans,
                authorizationCode, redirectUrl, scope, signer);
        return convertToOAuthToken(userId, accessToken, redirectUrl, createdDate);
    }

    @Override
    public AccessMeans getClientAccessToken(HttpClient httpClient,
                                            DefaultAuthMeans authenticationMeans,
                                            AuthenticationMeansReference authenticationMeansReference,
                                            TokenScope scope,
                                            Signer signer) {
        TokenKey tokenKey = new TokenKey(scope, authenticationMeansReference);
        if (clientAccessTokenIsEmptyOrExpired(tokenKey)) {
            refreshClientAccessToken(httpClient, authenticationMeans, authenticationMeansReference, scope, signer);
        }

        return clientAccessTokens.get(tokenKey).getClientAccessToken();
    }

    @Override
    public AccessMeans refreshAccessToken(HttpClient httpClient,
                                          DefaultAuthMeans authenticationMeans,
                                          UUID userId,
                                          String refreshToken,
                                          String redirectUrl,
                                          TokenScope scope,
                                          Signer signer) throws TokenInvalidException {
        AccessTokenResponseDTO accessToken = oauth2Client.refreshAccessToken(httpClient, authenticationMeans,
                refreshToken, redirectUrl, scope, signer);
        return convertToOAuthToken(userId, accessToken, redirectUrl);
    }

    protected String generateUserRequestToken(DefaultAuthMeans authenticationMeans,
                                              String resourceId,
                                              String secretState,
                                              String redirectUrl,
                                              Signer signer,
                                              TokenScope scope,
                                              String... args) throws JoseException {
        JwtClaims claims = tokenClaimsProducer.createUserRequestTokenClaims(authenticationMeans, resourceId, secretState, redirectUrl, scope, args);
        return userRequestTokenSigner.sign(authenticationMeans, claims, signer);
    }

    protected AccessMeans convertToOAuthToken(UUID userId,
                                              AccessTokenResponseDTO accessTokenResponseDTO,
                                              String redirectURI,
                                              Instant createDate) {
        final long expiresInSeconds = accessTokenResponseDTO.getExpiresIn();
        final Instant expireInstant = Instant.now(clock).plusSeconds(expiresInSeconds);

        final AccessMeans oAuthToken = new AccessMeans();
        oAuthToken.setUserId(userId);
        oAuthToken.setAccessToken(accessTokenResponseDTO.getAccessToken());
        oAuthToken.setExpireTime(Date.from(expireInstant));
        oAuthToken.setUpdated(new Date());
        oAuthToken.setRefreshToken(accessTokenResponseDTO.getRefreshToken());
        oAuthToken.setRedirectUri(redirectURI);
        oAuthToken.setCreated(createDate);

        return oAuthToken;
    }

    protected AccessMeans convertToOAuthToken(UUID userId,
                                              AccessTokenResponseDTO accessTokenResponseDTO,
                                              String redirectURI) {
        return convertToOAuthToken(userId, accessTokenResponseDTO, redirectURI, Instant.ofEpochMilli(0L));
    }

    protected boolean clientAccessTokenIsEmptyOrExpired(TokenKey openBankingTokenKey) {
        TokenEntry tokenEntry = clientAccessTokens.get(openBankingTokenKey);
        if (tokenEntry == null || tokenEntry.getClientAccessToken() == null) {
            return true;
        }

        // Just 10 seconds slack for network latency etc. so we are sure that the token is 'at least' another 10 sec
        // valid
        Instant saveExpiryTime = tokenEntry.getClientAccessToken().getExpireTime().toInstant().minusSeconds(10);
        return saveExpiryTime.isBefore(Instant.now(clock));
    }

    protected void refreshClientAccessToken(HttpClient httpClient,
                                            DefaultAuthMeans authenticationMeans,
                                            AuthenticationMeansReference authenticationMeansReference,
                                            TokenScope scope,
                                            Signer signer) {
        TokenKey tokenKey = new TokenKey(scope, authenticationMeansReference);
        TokenEntry tokenEntry = clientAccessTokens.computeIfAbsent(tokenKey, reference -> new TokenEntry());
        ReentrantLock lock = tokenEntry.getLock();
        if (lock.tryLock()) {
            // We got the refreshClientSessionLock.
            try {
                AccessMeans accessMeans = doClientCredentialsGrant(httpClient, authenticationMeans, scope, signer);
                tokenEntry.setClientAccessToken(accessMeans);
            } catch (Exception e) {
                throw new ClientCredentialFailedException("creating client session (client credentials grant) failed.", e);
            } finally {
                lock.unlock();
            }
        } else {
            // Another thread handles refreshing the token. Wait for that.
            try {
                lock.lock();
            } finally {
                lock.unlock();
            }
        }
    }

    private AccessMeans doClientCredentialsGrant(final HttpClient httpClient,
                                                 final DefaultAuthMeans authenticationMeans,
                                                 final TokenScope scope,
                                                 final Signer signer) {
        try {
            AccessTokenResponseDTO accessToken = oauth2Client.createClientCredentials(httpClient, authenticationMeans, scope, signer);
            return convertToOAuthToken(null, accessToken, null);

        } catch (HttpStatusCodeException e) {
            throw new GetAccessTokenFailedException(String.format("Could not set up client session. Received error response for client-credentials grant. Code: %s. Check RDD for body.",
                    e.getStatusCode().toString()));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessMeans getClientAccessTokenWithoutCache(final HttpClient httpClient,
                                                        final DefaultAuthMeans authenticationMeans,
                                                        final TokenScope scope,
                                                        final Signer signer) {
        return doClientCredentialsGrant(httpClient, authenticationMeans, scope, signer);
    }

    @Data
    protected class TokenKey {

        private final TokenScope scope;
        private final AuthenticationMeansReference authenticationMeansReference;
    }

    /**
     * It represents {@link AccessMeans} client access token with combination of ReentrantLock {@link ReentrantLock} that is used to
     * obtain that token. But due to fact that there are possible multiple authentication means for multiple client applications, we have
     * to keep and handle those clientAccessTokens (and their locks) separately, so we are storing those in a map and this class is value of this map.
     */
    @Data
    protected class TokenEntry {

        private final ReentrantLock lock = new ReentrantLock();
        private AccessMeans clientAccessToken;
    }
}
