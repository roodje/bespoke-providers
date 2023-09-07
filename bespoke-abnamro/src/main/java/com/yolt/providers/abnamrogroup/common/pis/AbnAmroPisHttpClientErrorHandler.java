package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

public class AbnAmroPisHttpClientErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) {
        throw e; // rethrow it and let it be handled by PEC Error Handler instead
    }
}
