package com.yolt.providers.stet.labanquepostalegroup.common.service.fetchdata.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.rest.error.DefaultFetchDataHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class LaBanquePostaleGroupFetchAccountsHttpErrorHandler extends DefaultFetchDataHttpErrorHandler {

    @Override
    public <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo) throws TokenInvalidException {
        try {
            return execution.get();
        } catch (HttpStatusCodeException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                // No response body will be treated as an empty account list
                return null;
            }
            handle(e, executionInfo);
            throw e;
        }
    }

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        super.handle(e, executionInfo);
    }
}
