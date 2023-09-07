package com.yolt.providers.openbanking.ais.virginmoney.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.*;

public final class VirginMoneyFetchDataHandlerV2 implements HttpErrorHandler {

    public static final VirginMoneyFetchDataHandlerV2 VIRGIN_MONEY_FETCH_DATA_HANDLER = new VirginMoneyFetchDataHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        HttpStatus httpStatus = e.getStatusCode();
        if (isHTMLWithBanInfo(e)) {
            throw e;
        }
        if (UNAUTHORIZED.equals(httpStatus) || FORBIDDEN.equals(httpStatus) || isInvalidGrant(e) || isRejectedConsent(e)) {
            throw new TokenInvalidException("Token invalid, received status {}.");
        } else {
            throw e;
        }
    }

    private boolean isHTMLWithBanInfo(final HttpStatusCodeException e) {
        return e.getResponseBodyAsString().contains("The owner of this website (secureapi.prod.ob.virginmoney.com) has banned your IP address");

    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains("invalid_grant");
    }

    private boolean isRejectedConsent(final HttpStatusCodeException e) {
        return BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains("Invalid Consent");
    }
}
