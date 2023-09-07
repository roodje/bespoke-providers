package com.yolt.providers.openbanking.ais.rbsgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class RbsGroupUserSiteDeleteHandlerV2 implements HttpErrorHandler {

    public static final RbsGroupUserSiteDeleteHandlerV2 RBS_GROUP_USER_SITE_DELETE_HANDLER = new RbsGroupUserSiteDeleteHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
            return;
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new TokenInvalidException(String.format("Received error code %s. Check RDD for body.", e.getStatusCode()));
        }
        throw e;
    }
}
