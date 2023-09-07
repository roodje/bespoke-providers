package com.yolt.providers.openbanking.ais.santander.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public final class SantanderHttpErrorHandlerV2 implements HttpErrorHandler {

    public static final SantanderHttpErrorHandlerV2 SANTANDER_HTTP_ERROR_HANDLER = new SantanderHttpErrorHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new TokenInvalidException(String.format("Received error code %s.", e.getStatusCode()));
        }
        throw e;
    }
}
