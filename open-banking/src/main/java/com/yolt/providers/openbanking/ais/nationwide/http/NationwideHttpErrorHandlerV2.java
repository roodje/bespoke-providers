package com.yolt.providers.openbanking.ais.nationwide.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.nationwide.NationwidePropertiesV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@RequiredArgsConstructor
public final class NationwideHttpErrorHandlerV2 implements HttpErrorHandler {

    private final NationwidePropertiesV2 nationwideProperties;

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isTokenInvalidException(e)) {
            throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode().toString()));
        } else {
            throw e;
        }
    }

    private boolean isTokenInvalidException(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                e.getStatusCode() == HttpStatus.FORBIDDEN ||
                isInvalidGrant(e);
    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains(nationwideProperties.getRefreshTokenExpiredMessage());
    }
}
