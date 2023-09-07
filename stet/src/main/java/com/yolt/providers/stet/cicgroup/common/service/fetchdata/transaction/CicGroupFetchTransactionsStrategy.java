package com.yolt.providers.stet.cicgroup.common.service.fetchdata.transaction;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;

import java.time.Period;

public class CicGroupFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    public CicGroupFetchTransactionsStrategy(FetchDataRestClient restClient, DateTimeSupplier dateTimeSupplier, DefaultProperties properties, Period fetchPeriod) {
        super(restClient, dateTimeSupplier, properties, fetchPeriod);
    }
}
