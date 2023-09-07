package com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http.errorhandler;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.errorhandler.ClosedAndBlockedAccountErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
public class HsbcClosedAndBlockedAccountErrorHandler implements ClosedAndBlockedAccountErrorHandler {

    private static final String ACCOUNT_BLOCKED_MESSAGE = "Failed Eligibility check";

    @Override
    public void handle(HttpStatusCodeException e) {
        if (!e.getStatusCode().equals(HttpStatus.BAD_REQUEST)
                || !e.getResponseBodyAsString().contains(ACCOUNT_BLOCKED_MESSAGE)) {
            throw e;
        }

    }
}
