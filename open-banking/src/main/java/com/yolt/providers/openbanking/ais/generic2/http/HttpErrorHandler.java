package com.yolt.providers.openbanking.ais.generic2.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.springframework.web.client.HttpStatusCodeException;

@FunctionalInterface
public interface HttpErrorHandler {
    void handle(HttpStatusCodeException e) throws TokenInvalidException;
}
