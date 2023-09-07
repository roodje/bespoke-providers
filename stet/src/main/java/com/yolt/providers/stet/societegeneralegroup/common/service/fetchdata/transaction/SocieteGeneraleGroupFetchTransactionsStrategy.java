package com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.transaction;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.SocieteGeneraleDateTimeSupplier;

import java.time.Instant;
import java.time.Period;

public class SocieteGeneraleGroupFetchTransactionsStrategy extends DefaultFetchTransactionsStrategy {

    private SocieteGeneraleDateTimeSupplier dateTimeSupplier;

    public SocieteGeneraleGroupFetchTransactionsStrategy(FetchDataRestClient restClient,
                                                         SocieteGeneraleDateTimeSupplier dateTimeSupplier,
                                                         DefaultProperties properties) {
        super(restClient, dateTimeSupplier, properties, Period.ofDays(180));
        this.dateTimeSupplier = dateTimeSupplier;
    }

    /**
     * Societe Generale introduced a new requirement - 'dateFrom' in transactions API should be not older than 6 months.
     * It also includes first data fetch
     * If we provide 'dateFrom' that is older, we will get HTTP 400
     */
    @Override
    protected Instant adjustFetchStartTime(DataRequest dataRequest, Instant fetchStartTime) {
        Instant adjustedFetchDate = dateTimeSupplier.getDefaultInstant().minus(Period.ofDays(180));
        if (fetchStartTime.isBefore(adjustedFetchDate)) {
            return adjustedFetchDate;
        }
        return fetchStartTime;
    }

    @Override
    protected String configureUrlForInitialFetch(String endpoint, Instant adjustedFestStartTime) {
        return endpoint + "?dateFrom=" + dateTimeSupplier.prepareDateFromQueryParam(adjustedFestStartTime);
    }
}