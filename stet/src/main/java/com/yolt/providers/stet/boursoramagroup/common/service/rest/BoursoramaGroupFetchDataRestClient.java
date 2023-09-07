package com.yolt.providers.stet.boursoramagroup.common.service.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.boursoramagroup.common.dto.BoursoramaGroupTransactionsResponseDTO;
import com.yolt.providers.stet.boursoramagroup.common.http.UserHashUtil;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataHttpHeadersFactory;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID;

public class BoursoramaGroupFetchDataRestClient extends DefaultFetchDataRestClient {

    public BoursoramaGroupFetchDataRestClient(FetchDataHttpHeadersFactory headersFactory) {
        super(headersFactory);
    }

    @Override
    public StetAccountsResponseDTO getAccounts(HttpClient httpClient, String endpoint, DataRequest dataRequest) throws TokenInvalidException {
        return super.getAccounts(httpClient, generateEndpoint(dataRequest, endpoint), dataRequest);
    }

    @Override
    public StetBalancesResponseDTO getBalances(HttpClient httpClient, String endpoint, DataRequest dataRequest) throws TokenInvalidException {
        return super.getBalances(httpClient, generateEndpoint(dataRequest, endpoint), dataRequest);
    }

    @Override
    public StetTransactionsResponseDTO getTransactions(HttpClient httpClient, String endpoint, DataRequest dataRequest) throws TokenInvalidException {
        return super.getData(httpClient, generateEndpoint(dataRequest, endpoint), dataRequest, GET_TRANSACTIONS_BY_ACCOUNT_ID, BoursoramaGroupTransactionsResponseDTO.class);
    }

    private String generateEndpoint(DataRequest dataRequest, String endpoint) throws TokenInvalidException {
        return String.format(endpoint, UserHashUtil.fromAccessMeans(dataRequest.getAccessToken()));
    }
}