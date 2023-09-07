package com.yolt.providers.stet.generic.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.FetchDataRequest;

public interface FetchDataService {

    DataProviderResponse getAccountsAndTransactions(HttpClient httpClient,
                                                    FetchDataRequest fetchDataRequest) throws ProviderFetchDataException, TokenInvalidException;
}
