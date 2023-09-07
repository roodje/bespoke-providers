package com.yolt.providers.bancatransilvania.common.http.error;

import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

public class BancaTransilvaniaRegistrationHttpErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) {
        throw new IllegalStateException("Registration failed. HTTP status: " + e.getRawStatusCode());
    }
}
