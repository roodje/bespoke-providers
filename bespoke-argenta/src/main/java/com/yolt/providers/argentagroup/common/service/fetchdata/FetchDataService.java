package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;

public interface FetchDataService {
    DataProviderResponse fetchData(UrlFetchDataRequest request,
                                   DefaultAuthenticationMeans authenticationMeans,
                                   HttpClient httpClient) throws TokenInvalidException, ProviderFetchDataException;
}
