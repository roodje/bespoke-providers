package com.yolt.providers.stet.generic.http.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

/**
 *
 */
@Deprecated
public class NoActionHttpErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) throws TokenInvalidException {
        throw e;
    }
}
