package com.yolt.providers.stet.labanquepostalegroup.common.service.fetchdata.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataHttpHeadersFactory;
import com.yolt.providers.stet.labanquepostalegroup.common.dto.LaBanquePostaleGroupTransactionsResponseDTO;
import com.yolt.providers.stet.labanquepostalegroup.common.service.fetchdata.error.LaBanquePostaleGroupFetchAccountsHttpErrorHandler;
import com.yolt.providers.stet.labanquepostalegroup.common.service.fetchdata.error.LaBanquePostaleGroupFetchBalancesAndTransactionsHttpErrorHandler;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;

public class LaBanquePostaleGroupFetchDataRestClient extends DefaultFetchDataRestClient {

    private static final HttpErrorHandler FETCH_ACCOUNTS_HTTP_ERROR_HANDLER = new LaBanquePostaleGroupFetchAccountsHttpErrorHandler();
    //TODO Remove as part of https://yolt.atlassian.net/browse/C4PO-8222
    private static final HttpErrorHandler FETCH_BALANCED_AND_TRANSACTION_HTTP_ERROR_HANDLER = new LaBanquePostaleGroupFetchBalancesAndTransactionsHttpErrorHandler();

    public LaBanquePostaleGroupFetchDataRestClient(FetchDataHttpHeadersFactory httpHeadersFactory) {
        super(httpHeadersFactory);
    }

    @Override
    public StetAccountsResponseDTO getAccounts(HttpClient httpClient, String endpoint, DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_ACCOUNTS, StetAccountsResponseDTO.class, FETCH_ACCOUNTS_HTTP_ERROR_HANDLER);
    }

    //TODO Remove this method as part of https://yolt.atlassian.net/browse/C4PO-8222
    @Override
    public StetBalancesResponseDTO getBalances(HttpClient httpClient,
                                               String endpoint,
                                               DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_BALANCES_BY_ACCOUNT_ID, StetBalancesResponseDTO.class, FETCH_BALANCED_AND_TRANSACTION_HTTP_ERROR_HANDLER);
    }

    @Override
    //TODO Remove error handler argument https://yolt.atlassian.net/browse/C4PO-8222
    public LaBanquePostaleGroupTransactionsResponseDTO getTransactions(HttpClient httpClient, String endpoint, DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_TRANSACTIONS_BY_ACCOUNT_ID, LaBanquePostaleGroupTransactionsResponseDTO.class, FETCH_BALANCED_AND_TRANSACTION_HTTP_ERROR_HANDLER);
    }
}
