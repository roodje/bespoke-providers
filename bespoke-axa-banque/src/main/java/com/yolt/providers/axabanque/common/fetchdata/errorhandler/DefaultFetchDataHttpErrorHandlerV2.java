package com.yolt.providers.axabanque.common.fetchdata.errorhandler;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@RequiredArgsConstructor
public class DefaultFetchDataHttpErrorHandlerV2 implements HttpErrorHandlerV2 {

    public static final String TOO_MANY_REQUESTS = "Too many requests: Http status ";

    private final HttpErrorHandlerV2 wrappee;

    @Override
    public void handle(HttpStatusCodeException e, Object additionalParameters) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        if (HttpStatus.TOO_MANY_REQUESTS.equals(status)) {
            String errorMessage = TOO_MANY_REQUESTS + status.value();
            if (ObjectUtils.isEmpty(additionalParameters)) {
                throw new BackPressureRequestException(errorMessage);
            }
            throw new ProviderHttpStatusException(errorMessage);
        }
        wrappee.handle(e, additionalParameters);
    }
}

