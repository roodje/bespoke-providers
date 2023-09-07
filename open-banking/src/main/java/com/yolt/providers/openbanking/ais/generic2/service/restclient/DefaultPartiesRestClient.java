package com.yolt.providers.openbanking.ais.generic2.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static com.yolt.providers.openbanking.ais.ProviderClientEndpoints.PARTIES;

public class DefaultPartiesRestClient implements PartiesRestClient {

    @Override
    public <T> T callForParties(final HttpClient httpClient,
                                final String exchangePath,
                                final AccessMeans clientAccessToken,
                                final DefaultAuthMeans authMeans,
                                final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(clientAccessToken)),
                PARTIES,
                responseType,
                DefaultHttpErrorHandler.DEFAULT_HTTP_ERROR_HANDLER).getBody();
    }

    protected HttpHeaders getHeaders(final AccessMeans clientAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
