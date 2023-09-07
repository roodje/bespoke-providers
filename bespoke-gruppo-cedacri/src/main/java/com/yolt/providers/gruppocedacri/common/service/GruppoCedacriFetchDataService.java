package com.yolt.providers.gruppocedacri.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClient;

import java.time.Instant;

public interface GruppoCedacriFetchDataService {

    DataProviderResponse fetchData(GruppoCedacriHttpClient httpClient,
                                   GruppoCedacriAccessMeans accessMeans,
                                   Instant transactionFetchStart,
                                   String psuIpAddress,
                                   String providerDisplayName) throws TokenInvalidException, ProviderFetchDataException;
}
