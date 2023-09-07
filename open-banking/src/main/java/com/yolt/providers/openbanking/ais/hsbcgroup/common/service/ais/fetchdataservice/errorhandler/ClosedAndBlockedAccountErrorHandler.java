package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.errorhandler;

import org.springframework.web.client.HttpStatusCodeException;
public interface ClosedAndBlockedAccountErrorHandler {

    void handle(HttpStatusCodeException e);
}
