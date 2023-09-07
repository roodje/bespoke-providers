package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataHttpHeadersFactory;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID;

public class LclFetchDataRestClient extends DefaultFetchDataRestClient {

    private static final String REQUEST_TARGET_ENDPOINT_PREFIX = "/aisp/1.1";

    public LclFetchDataRestClient(final FetchDataHttpHeadersFactory headersFactory, final HttpErrorHandler errorHandler) {
        super(headersFactory, errorHandler);
    }

    @Override
    public StetTransactionsResponseDTO getTransactions(HttpClient httpClient,
                                                       String endpoint,
                                                       DataRequest dataRequest) throws TokenInvalidException {
        String endpointWithoutHostName = endpoint.substring(endpoint.indexOf(REQUEST_TARGET_ENDPOINT_PREFIX));
        return getData(httpClient, endpointWithoutHostName, dataRequest, GET_TRANSACTIONS_BY_ACCOUNT_ID, StetTransactionsResponseDTO.class);
    }
}
