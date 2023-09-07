package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.errorhandler;

import org.springframework.web.client.HttpStatusCodeException;
public class DefaultClosedAndBlockedErrorHandler implements ClosedAndBlockedAccountErrorHandler {
    @Override
    public void handle(HttpStatusCodeException e) {
        throw  e;
    }
}
