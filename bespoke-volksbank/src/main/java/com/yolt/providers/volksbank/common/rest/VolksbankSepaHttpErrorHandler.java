package com.yolt.providers.volksbank.common.rest;

import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

public class VolksbankSepaHttpErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) {
        throw e;
    }
}
