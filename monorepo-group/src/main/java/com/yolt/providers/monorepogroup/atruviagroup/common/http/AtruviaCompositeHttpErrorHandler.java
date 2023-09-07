package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;

public class AtruviaCompositeHttpErrorHandler implements HttpErrorHandlerV2 {

    private final List<HttpErrorHandlerV2> httpErrorHandlers;

    public AtruviaCompositeHttpErrorHandler(HttpErrorHandlerV2... httpErrorHandler) {
        this.httpErrorHandlers = Arrays.asList(httpErrorHandler);
    }

    @Override
    public void handle(HttpStatusCodeException e, Object param) throws TokenInvalidException {
        for (HttpErrorHandlerV2 httpErrorHandler : httpErrorHandlers) {
            httpErrorHandler.handle(e, param);
        }
    }
}
