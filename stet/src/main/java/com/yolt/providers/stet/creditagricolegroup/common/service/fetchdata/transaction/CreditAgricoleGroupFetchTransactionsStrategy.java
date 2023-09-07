package com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.transaction;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;

import java.time.Instant;
import java.time.Period;

public class CreditAgricoleGroupFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    public CreditAgricoleGroupFetchTransactionsStrategy(FetchDataRestClient restClient,
                                                        DateTimeSupplier dateTimeSupplier,
                                                        DefaultProperties properties) {
        super(restClient, dateTimeSupplier, properties);
    }

    @Override
    protected Instant adjustFetchStartTime(DataRequest dataRequest, Instant fetchStartTime) {
        Instant eightyNineDaysAgo = dateTimeSupplier.getDefaultInstant().minus(Period.ofDays(89));
        if (dataRequest.isRefreshedToken() && fetchStartTime.isBefore(eightyNineDaysAgo)) {
            return eightyNineDaysAgo;
        }
        return fetchStartTime;
    }
}
