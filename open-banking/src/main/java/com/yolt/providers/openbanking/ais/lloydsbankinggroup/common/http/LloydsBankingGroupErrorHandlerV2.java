package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class LloydsBankingGroupErrorHandlerV2 implements HttpErrorHandler {

    public static final LloydsBankingGroupErrorHandlerV2 LLOYDS_BANKING_GROUP_ERROR_HANDLER = new LloydsBankingGroupErrorHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (HttpStatus.FORBIDDEN.equals(e.getStatusCode()) || HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
            throw new TokenInvalidException(String.format("Received error code %s. Check RDD for body.", e.getStatusCode()));
        } else {
            throw e;
        }
    }
}
