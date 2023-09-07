package com.yolt.providers.cbiglobe.common.pis.pec.auth;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.model.TokenResponse;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePisHttpHeadersFactory;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClient;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class CbiGlobePisAuthenticationService {

    private final CbiGlobePisHttpHeadersFactory httpHeadersFactory;
    private final CbiGlobeBaseProperties properties;
    private final Map<TokenKey, TokenEntry> clientAccessTokens = new ConcurrentHashMap<>();
    private final Clock clock;

    public Token getClientAccessToken(CbiGlobePisHttpClient httpClient,
                                      CbiGlobeAuthenticationMeans authenticationMeans,
                                      AuthenticationMeansReference authenticationMeansReference) {
        TokenKey tokenKey = new TokenKey(authenticationMeansReference);
        if (isClientAccessTokenEmptyOrExpired(tokenKey)) {
            refreshClientAccessToken(httpClient, authenticationMeans, authenticationMeansReference);
        }

        return clientAccessTokens.get(tokenKey).getClientAccessTokenData();
    }

    private boolean isClientAccessTokenEmptyOrExpired(TokenKey tokenKey) {
        var tokenEntry = clientAccessTokens.get(tokenKey);
        if (tokenEntry == null || tokenEntry.getClientAccessTokenData() == null) {
            return true;
        }
        return tokenEntry.getExpiresIn().minusSeconds(10).isBefore(Instant.now(clock));
    }

    private void refreshClientAccessToken(CbiGlobePisHttpClient httpClient,
                                            CbiGlobeAuthenticationMeans authenticationMeans,
                                            AuthenticationMeansReference authenticationMeansReference) {
        var tokenKey = new TokenKey(authenticationMeansReference);
        var tokenEntry = clientAccessTokens.computeIfAbsent(tokenKey, reference -> new TokenEntry());
        ReentrantLock lock = tokenEntry.getLock();
        if (lock.tryLock()) {
            try {
                Token token = getTokenOnClientCredentialsGrant(httpClient, authenticationMeans);
                tokenEntry.setClientAccessTokenData(token);
            } catch (RestClientResponseException e) {
                throw new GetAccessTokenFailedException("Client credentials grant failed: HTTP: " + e.getRawStatusCode());
            } catch (Exception e) {
                throw new GetAccessTokenFailedException("Creating client session (client credentials grant) failed.");
            } finally {
                lock.unlock();
            }
        } else {
            try {
                lock.lock();
            } finally {
                lock.unlock();
            }
        }
    }

    private Token getTokenOnClientCredentialsGrant(CbiGlobePisHttpClient httpClient,
                                                   CbiGlobeAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        HttpHeaders clientCredentialsHeaders = httpHeadersFactory.createClientCredentialsHeaders(authenticationMeans.getClientId(), authenticationMeans.getClientSecret());

        String tokenUrl = UriComponentsBuilder.fromUriString(properties.getTokenUrl())
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "production")
                .toUriString();

        TokenResponse tokenResponse = httpClient.getAccessToken(tokenUrl, clientCredentialsHeaders);

        return Token.from(tokenResponse, clock);
    }

    @Data
    private class TokenKey {

        private final AuthenticationMeansReference authenticationMeansReference;
    }

    /**
     * It represents {@link Token} client access token with combination of ReentrantLock {@link ReentrantLock} that is used to
     * obtain that token. But due to fact that there are possible multiple authentication means for multiple client applications, we have
     * to keep and handle those clientAccessTokens (and their locks) separately, so we are storing those in a map and this class is value of this map.
     */
    @Data
    private class TokenEntry {

        private final ReentrantLock lock = new ReentrantLock();
        private Token clientAccessTokenData;

        Instant getExpiresIn() {
            return clientAccessTokenData.getExpiresIn();
        }
    }
}
