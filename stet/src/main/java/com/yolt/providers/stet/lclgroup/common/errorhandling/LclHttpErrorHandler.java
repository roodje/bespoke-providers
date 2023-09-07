package com.yolt.providers.stet.lclgroup.common.errorhandling;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@RequiredArgsConstructor
public class LclHttpErrorHandler extends DefaultHttpErrorHandler {

    private static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                if (e.getResponseBodyAsString().contains(REFRESH_TOKEN_EXPIRED)) {
                    throw new TokenInvalidException("Http error status code: " + status);
                }
                throw new ProviderHttpStatusException("Request formed incorrectly: HTTP " + status.value());
            case UNAUTHORIZED:
            case FORBIDDEN:
                throw new TokenInvalidException("Http error status code: " + status);
            case INTERNAL_SERVER_ERROR:
                throw new ProviderHttpStatusException("Something went wrong on bank side: HTTP " + status.value());
            default:
                throw new ProviderHttpStatusException("Unknown exception: HTTP " + status.value());
        }
    }
}
