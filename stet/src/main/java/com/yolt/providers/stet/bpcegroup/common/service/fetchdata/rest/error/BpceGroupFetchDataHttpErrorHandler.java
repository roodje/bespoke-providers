package com.yolt.providers.stet.bpcegroup.common.service.fetchdata.rest.error;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

public class BpceGroupFetchDataHttpErrorHandler extends DefaultHttpErrorHandler {

    private static final String NO_ELIGIBLE_OR_AVAILABLE_ACCOUNTS = "NAAC";

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        if (TOO_MANY_REQUESTS.equals(status)) {
            throw new BackPressureRequestException("HTTP:429 Too Many Requests");
        }
        if (NOT_FOUND.equals(status) && e.getResponseBodyAsString().contains(NO_ELIGIBLE_OR_AVAILABLE_ACCOUNTS)) {
            throw new ProviderHttpStatusException("NAAC No available accounts : no eligible or authorized accounts", e);
        }
        super.handle(e, executionInfo);
    }
}
