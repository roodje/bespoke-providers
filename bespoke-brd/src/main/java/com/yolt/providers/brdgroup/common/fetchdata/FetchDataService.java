package com.yolt.providers.brdgroup.common.fetchdata;

import com.yolt.providers.brdgroup.common.BrdGroupAccessMeans;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;

import java.time.Instant;

public interface FetchDataService {

    DataProviderResponse fetchData(BrdGroupHttpClient brdGroupHttpClient,
                                   BrdGroupAccessMeans brdGroupAccessMeans,
                                   Instant transactionFetchStart,
                                   String psuIpAddress,
                                   String providerDisplayName) throws ProviderFetchDataException;
}
