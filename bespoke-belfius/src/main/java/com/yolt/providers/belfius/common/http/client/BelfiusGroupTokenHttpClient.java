package com.yolt.providers.belfius.common.http.client;

import com.yolt.providers.belfius.common.auth.BelfiusGroupAuthMeans;
import com.yolt.providers.belfius.common.configuration.BelfiusBaseProperties;
import com.yolt.providers.belfius.common.http.HttpErrorHandler;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessToken;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class BelfiusGroupTokenHttpClient {

    private static final String GRANT_TYPE = "grant_type";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CODE = "code";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final RestTemplate restTemplate;
    private final BelfiusGroupAuthMeans authMeans;
    private final BelfiusBaseProperties properties;

    public BelfiusGroupAccessToken getAccessToken(String redirectUri, String code, String codeVerifier) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(authMeans.getClientId(), authMeans.getClientSecret());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.ACCEPT, properties.getAcceptHeaderValue());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "authorization_code");
        body.add(REDIRECT_URI, redirectUri);
        body.add(CODE, code);
        body.add(CODE_VERIFIER, codeVerifier);

        return restTemplate.exchange("/token",
                HttpMethod.POST,
                new HttpEntity<>(body, httpHeaders),
                BelfiusGroupAccessToken.class).getBody();
    }

    public BelfiusGroupAccessToken getAccessTokenUsingRefreshToken(String refreshToken) throws TokenInvalidException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(authMeans.getClientId(), authMeans.getClientSecret());
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.ACCEPT, properties.getAcceptHeaderValue());

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, refreshToken);

        return getData("/token",
                HttpMethod.POST,
                new HttpEntity<>(body, httpHeaders),
                BelfiusGroupAccessToken.class,
                Collections.emptyMap());
    }

    private <T> T getData(String url, HttpMethod httpMethod, HttpEntity httpEntity, Class<T> responseType, Map<String, String> uriVariables) throws TokenInvalidException {
        try {
            return restTemplate.exchange(url, httpMethod, httpEntity, responseType, uriVariables).getBody();
        } catch (HttpStatusCodeException e) {
            HttpErrorHandler.handleNon2xxResponseCode(e.getStatusCode());
            throw e;
        }
    }
}
