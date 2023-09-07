package com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.error;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.rest.error.DefaultFetchDataHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.concurrent.TimeUnit;

public class CreditAgricoleGroupFetchDataHttpErrorHandler extends DefaultFetchDataHttpErrorHandler {
    //Will be verified in C4PO-8931
    private static final String SUSPENDED_ERROR_CODE = "303001";
    private static final int MAX_RETRIES = 9;
    private static final int TIME_TO_REPEAT_CALL_IN_SECONDS = 3;
    private static final String FETCH_DATA_ERROR_MESSAGE = "Creditagricole fetch data error";

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        if (HttpStatus.FORBIDDEN.equals(status) && isFlywheelExecution(executionInfo)) {
            throw new BackPressureRequestException("Access to call is forbidden: HTTP " + status.value());
        }
        super.handle(e, executionInfo);
    }

    @Override
    public <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo) throws TokenInvalidException {
        return executeAndHandle(execution, executionInfo, MAX_RETRIES);
    }

    private <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo, int retryLimit) throws TokenInvalidException {
        T result = null;
        try {
            return execution.get();
        } catch (HttpStatusCodeException e) {
            if (isSuspendedError(e) && retryLimit > 0) {
                waitSeconds(TIME_TO_REPEAT_CALL_IN_SECONDS);
                result = this.executeAndHandle(execution, executionInfo, --retryLimit);
            } else handle(e, executionInfo);
        }
        return result;
    }

    private void waitSeconds(int seconds) throws TokenInvalidException {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new TokenInvalidException(FETCH_DATA_ERROR_MESSAGE);
        }
    }

    private boolean isSuspendedError(HttpStatusCodeException exception) {
        String body = exception.getResponseBodyAsString();

        return HttpStatus.INTERNAL_SERVER_ERROR.equals(exception.getStatusCode())
                && body.contains(SUSPENDED_ERROR_CODE);
    }
}
