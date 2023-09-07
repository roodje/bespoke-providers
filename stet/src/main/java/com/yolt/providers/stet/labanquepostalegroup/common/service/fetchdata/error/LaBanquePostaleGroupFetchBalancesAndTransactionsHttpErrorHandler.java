package com.yolt.providers.stet.labanquepostalegroup.common.service.fetchdata.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.fetchdata.rest.error.DefaultFetchDataHttpErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Optional;

@Slf4j
//TODO Remove as part of https://yolt.atlassian.net/browse/C4PO-8222
public class LaBanquePostaleGroupFetchBalancesAndTransactionsHttpErrorHandler extends DefaultFetchDataHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
            Optional.ofNullable(e.getResponseBodyAsString()).ifPresent(message -> {
                if (message.contains("404 File Not Found")) {
                    log.info("404 case on balances or transactions endpoint occurred. Check details in rdd");
                }
            });
        }
        super.handle(e, executionInfo);
    }
}
