package com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.creditagricolegroup.common.dto.CreditAgricoleGroupTransactionsResponseDTO;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataHttpHeadersFactory;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID;

public class CreditAgricoleGroupFetchDataRestClient extends DefaultFetchDataRestClient {

    public CreditAgricoleGroupFetchDataRestClient(FetchDataHttpHeadersFactory headersFactory, HttpErrorHandler errorHandler) {
        super(headersFactory, errorHandler);
    }

    @Override
    public CreditAgricoleGroupTransactionsResponseDTO getTransactions(HttpClient httpClient, String endpoint, DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_TRANSACTIONS_BY_ACCOUNT_ID, CreditAgricoleGroupTransactionsResponseDTO.class);
    }
}
