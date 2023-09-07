package com.yolt.providers.stet.boursoramagroup.common.http.error;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;

public class BoursoramaHttpErrorHandler implements HttpErrorHandler {

    @Override
    public <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo) throws TokenInvalidException {
        try {
            return execution.get();
        } catch (HttpStatusCodeException e) {
            handle(e, executionInfo);
            throw e;
        }
    }

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                if (isRefreshTokenExpired(e)) {
                    throw new TokenInvalidException("Refresh token is expired", e);
                }
                throw new ProviderHttpStatusException("Request formed incorrectly: HTTP " + status.value());
            case UNAUTHORIZED:
                throw new TokenInvalidException("We are not authorized to call endpoint: HTTP " + status.value());
            case FORBIDDEN:
                throw new TokenInvalidException("Access to call is forbidden: HTTP " + status.value());
            case NOT_FOUND:
                throw new TokenInvalidException("Request about token not found: HTTP " + status.value());
            case INTERNAL_SERVER_ERROR:
                throw new ProviderHttpStatusException("Something went wrong on bank side: HTTP " + status.value());
            default:
                throw new ProviderHttpStatusException("Unknown exception: HTTP " + status.value());
        }
    }

    private boolean isRefreshTokenExpired(HttpStatusCodeException e) {
        String responseBody = e.getResponseBodyAsString();
        return StringUtils.hasText(responseBody)
                && responseBody.contains("Refresh Token expired");
    }
}
