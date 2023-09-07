package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

public class DefaultPisHttpClientErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) {
        throw e; // rethrow it and let it be handled by PEC Error Handler instead
    }
}
