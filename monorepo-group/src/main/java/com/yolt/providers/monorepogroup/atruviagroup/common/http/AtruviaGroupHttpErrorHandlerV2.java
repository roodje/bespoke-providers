package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class AtruviaGroupHttpErrorHandlerV2 implements HttpErrorHandlerV2 {

    @Override
    public void handle(HttpStatusCodeException e, Object param) throws TokenInvalidException {
        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            var responseBodyAsString = e.getResponseBodyAsString();
            if (responseBodyAsString.contains("CONSENT_UNKNOWN") && responseBodyAsString.contains("ERROR")) {
                throw new TokenInvalidException("Consent is invalid.");
            }
        }
    }
}
