package com.yolt.providers.openbanking.ais.tescobank.service.ais.exceptionhandler;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler.FetchDataExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@RequiredArgsConstructor
public class TescoFetchAccountsExceptionHandler implements FetchDataExceptionHandler {

    private static final String ACCOUNT_BLOCKED_CODE = "UK.TB.OB.InvalidAccount";
    private static final String ACCOUNT_BLOCKED_MESSAGE = "Invalid Account Status";
    private final FetchDataExceptionHandler wrappee;

    @Override
    public void handleException(RuntimeException e) throws ProviderFetchDataException, TokenInvalidException {
        if (e instanceof HttpStatusCodeException && ((HttpStatusCodeException) e).getStatusCode().equals(HttpStatus.BAD_REQUEST) &&
                ((HttpStatusCodeException) e).getResponseBodyAsString().contains(ACCOUNT_BLOCKED_CODE) &&
                ((HttpStatusCodeException) e).getResponseBodyAsString().contains(ACCOUNT_BLOCKED_MESSAGE)) {
            throw new TokenInvalidException("User account has invalid status");
        }
        wrappee.handleException(e);
    }
}
