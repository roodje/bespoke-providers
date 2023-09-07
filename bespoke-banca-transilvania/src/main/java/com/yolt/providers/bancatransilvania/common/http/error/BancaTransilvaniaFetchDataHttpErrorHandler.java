package com.yolt.providers.bancatransilvania.common.http.error;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandler;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

public class BancaTransilvaniaFetchDataHttpErrorHandler implements HttpErrorHandler {

    static final DefaultHttpErrorHandler DEFAULT_HTTP_ERROR_HANDLER = new DefaultHttpErrorHandler();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        if (TOO_MANY_REQUESTS.equals(status)) {
            throw new BackPressureRequestException(status.getReasonPhrase() + " " + status.value());
        }
        DEFAULT_HTTP_ERROR_HANDLER.handle(e);
    }
}
