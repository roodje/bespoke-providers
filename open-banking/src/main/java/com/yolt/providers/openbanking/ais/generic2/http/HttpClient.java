package com.yolt.providers.openbanking.ais.generic2.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpErrorHandler.DEFAULT_HTTP_ERROR_HANDLER;

public interface HttpClient {

    <T> ResponseEntity<T> exchange(final String endpoint,
                                   final HttpMethod method,
                                   final HttpEntity body,
                                   final String prometheusPathOverride,
                                   final Class<T> responseType,
                                   final HttpErrorHandler errorHandler,
                                   final String... uriArgs) throws TokenInvalidException;

    default <T> ResponseEntity<T> exchange(final String endpoint,
                                           final HttpMethod method,
                                           final HttpEntity body,
                                           final String prometheusPathOverride,
                                           final Class<T> responseType,
                                           final String... uriArgs) throws TokenInvalidException {
        return exchange(endpoint, method, body, prometheusPathOverride, responseType,
                DEFAULT_HTTP_ERROR_HANDLER, uriArgs);
    }
}