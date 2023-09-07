package com.yolt.providers.stet.generic.http.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import org.springframework.web.client.HttpStatusCodeException;

/**
 *
 */
public class NoActionHttpErrorHandlerV2 implements HttpErrorHandlerV2 {

    @Override
    public void handle(HttpStatusCodeException e, Object param) throws TokenInvalidException {
        throw e;
    }
}
