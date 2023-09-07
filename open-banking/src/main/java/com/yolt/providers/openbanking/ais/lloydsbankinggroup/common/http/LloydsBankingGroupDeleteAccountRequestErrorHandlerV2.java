package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http;

import com.yolt.providers.common.exception.ProviderRequestFailedException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class LloydsBankingGroupDeleteAccountRequestErrorHandlerV2 implements HttpErrorHandler {

    public static final LloydsBankingGroupDeleteAccountRequestErrorHandlerV2 LLOYDS_BANKING_GROUP_DELETE_ACCOUNT_REQUEST_ERROR_HANDLERERROR_HANDLER = new LloydsBankingGroupDeleteAccountRequestErrorHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) {
        if (e.getStatusCode().is5xxServerError()) {
            throw e;
        }
        if (e.getStatusCode() != HttpStatus.BAD_REQUEST && e.getStatusCode() != HttpStatus.NOT_FOUND) {
            String msg = String.format("Unable to delete account-request for Lloyds group. Received error code %s. Check RDD for body.",
                    e.getStatusCode());
            throw new ProviderRequestFailedException(msg);
        }
    }
}