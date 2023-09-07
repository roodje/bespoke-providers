package com.yolt.providers.nutmeggroup.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.nutmeggroup.common.OAuthConstants;
import com.yolt.providers.nutmeggroup.common.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.nutmeggroup.common.utils.HttpErrorHandler.handleNon2xxResponseCode;


@RequiredArgsConstructor
public class HttpClient {

    private final RestTemplate restTemplate;

    public PotsResponse getPots(final String bearer,
                         final String absoluteUrl) throws TokenInvalidException {
        HttpHeaders headers = createDefaultHeaders();
        headers.setBearerAuth(bearer);

        PotsResponse result = null;
        try {
            result = restTemplate.exchange(
                    absoluteUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    PotsResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseCode(e.getStatusCode());
        }
        return result;
    }

    public TokenResponse getAccessToken(final String clientId,
                                        final String code,
                                        final String codeVerifier,
                                        final String redirectUri,
                                        final String absoluteUrl) throws TokenInvalidException {
        HttpHeaders headers = createDefaultHeaders();

        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        accessTokenRequest.setGrantType(OAuthConstants.Values.AUTHORIZATION_CODE);
        accessTokenRequest.setClientId(clientId);
        accessTokenRequest.setCodeVerifier(codeVerifier);
        accessTokenRequest.setCode(code);
        accessTokenRequest.setRedirectUri(redirectUri);

        return postForEntity(absoluteUrl, accessTokenRequest, headers);
    }

    public TokenResponse refreshAccessToken(final String clientId,
                                            final String refreshToken,
                                            final String absoluteUrl) throws TokenInvalidException {
        HttpHeaders headers = createDefaultHeaders();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setGrantType(OAuthConstants.Values.REFRESH_TOKEN);
        refreshTokenRequest.setClientId(clientId);
        refreshTokenRequest.setRefreshToken(refreshToken);

        return postForEntity(absoluteUrl, refreshTokenRequest, headers);
    }

    private <T> TokenResponse postForEntity(String absoluteUrl,
                                            T request,
                                            HttpHeaders headers) throws TokenInvalidException {
        TokenResponse result = null;
        try {
            result = restTemplate.postForEntity(
                    absoluteUrl,
                    new HttpEntity<>(request, headers),
                    TokenResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseCode(e.getStatusCode());
        }
        return result;
    }

    private HttpHeaders createDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }


}
