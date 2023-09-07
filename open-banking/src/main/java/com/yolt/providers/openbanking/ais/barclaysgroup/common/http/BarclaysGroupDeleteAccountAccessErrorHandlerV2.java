package com.yolt.providers.openbanking.ais.barclaysgroup.common.http;

import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BarclaysGroupDeleteAccountAccessErrorHandlerV2 implements HttpErrorHandler {

    @Override
    public void handle(final HttpStatusCodeException e) {
        if (e.getStatusCode() != HttpStatus.FORBIDDEN) {
            throw e;
        }
    }
}
