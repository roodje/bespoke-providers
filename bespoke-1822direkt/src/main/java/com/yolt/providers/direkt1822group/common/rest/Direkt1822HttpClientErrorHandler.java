package com.yolt.providers.direkt1822group.common.rest;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class Direkt1822HttpClientErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) {
        if (HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode())) {
            throw new BackPressureRequestException("Daily limit for fetch data has been used");
        }
        throw e;
    }
}
