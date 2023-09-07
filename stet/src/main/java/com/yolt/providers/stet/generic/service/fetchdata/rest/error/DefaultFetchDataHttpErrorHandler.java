package com.yolt.providers.stet.generic.service.fetchdata.rest.error;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.PSU_IP_ADDRESS;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

//TODO: C4PO-7223 Remove this class and migrate to new solution, when ticket will be completed
public class DefaultFetchDataHttpErrorHandler extends DefaultHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        if (TOO_MANY_REQUESTS.equals(status)) {
            if (isFlywheelExecution(executionInfo)) {
                throw new BackPressureRequestException("Too many requests invoked without psu-ip-address present: HTTP " + status.value());
            } else {
                throw new TokenInvalidException("Too many requests invoked with psu-ip-address present: HTTP " + status.value());
            }
        }
        super.handle(e, executionInfo);
    }

    protected boolean isFlywheelExecution(ExecutionInfo executionInfo) {
        return !executionInfo.getHttpHeaders().containsKey(PSU_IP_ADDRESS);
    }
}
