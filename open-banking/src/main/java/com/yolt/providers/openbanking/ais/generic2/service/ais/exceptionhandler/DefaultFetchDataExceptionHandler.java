package com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import org.springframework.web.client.RestClientException;

public class DefaultFetchDataExceptionHandler implements FetchDataExceptionHandler {

    @Override
    public void handleException(RuntimeException exception) throws ProviderFetchDataException {
        if (exception instanceof RestClientException) {
            throw new ProviderFetchDataException(exception);
        }
    }
}
