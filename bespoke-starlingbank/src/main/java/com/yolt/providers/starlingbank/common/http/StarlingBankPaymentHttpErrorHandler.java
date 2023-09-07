package com.yolt.providers.starlingbank.common.http;

import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

public class StarlingBankPaymentHttpErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e) {
        throw e;
    }
}
