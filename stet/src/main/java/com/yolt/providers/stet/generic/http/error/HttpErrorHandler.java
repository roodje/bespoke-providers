package com.yolt.providers.stet.generic.http.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import org.springframework.web.client.HttpStatusCodeException;

//TODO: C4PO-7223 Remove this class and migrate to new solution, when ticket will be completed
public interface HttpErrorHandler {

    <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo) throws TokenInvalidException;

    void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException;
}
