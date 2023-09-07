package com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;

public interface FetchDataExceptionHandler {

    void handleException(RuntimeException e) throws ProviderFetchDataException, TokenInvalidException;
}
