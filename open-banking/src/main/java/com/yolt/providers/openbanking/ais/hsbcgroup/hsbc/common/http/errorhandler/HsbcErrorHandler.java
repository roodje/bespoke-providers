package com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http.errorhandler;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http.SupplierWithTokenInvalidException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.concurrent.TimeUnit;

public final class HsbcErrorHandler {
    public static final HsbcErrorHandler HSBC_ERROR_HANDLER = new HsbcErrorHandler();
    private static final int TIME_TO_REPEAT_CALL_IN_SECONDS = 10;
    private static final String NULL_MESSAGE_FIELD = "{\"message\":null}";
    private static final String FETCH_DATA_ERROR_MESSAGE = "HSBC fetch data error";

    public <T> T executeAndHandle(SupplierWithTokenInvalidException execution) throws TokenInvalidException {
        try {
            return (T) execution.get();
        } catch (HttpStatusCodeException e) {
            if (e.getResponseBodyAsString().contains(NULL_MESSAGE_FIELD)) {
                waitSeconds(TIME_TO_REPEAT_CALL_IN_SECONDS);
                return (T) execution.get();
            } else {
                throw e;
            }
        }
    }

    private void waitSeconds(int seconds) throws TokenInvalidException {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new TokenInvalidException(FETCH_DATA_ERROR_MESSAGE);
        }
    }
}
