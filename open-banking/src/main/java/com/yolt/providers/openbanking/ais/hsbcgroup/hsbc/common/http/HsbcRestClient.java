package com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.restclient.DefaultHsbcGroupRestClientV5;
import com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http.errorhandler.HsbcErrorHandler;

public class HsbcRestClient extends DefaultHsbcGroupRestClientV5 {
    public HsbcRestClient(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    HsbcErrorHandler hsbcErrorHandler = HsbcErrorHandler.HSBC_ERROR_HANDLER;

    @Override
    public <T> T fetchAccounts(HttpClient httpClient, String currentPath, AccessMeans accessToken, String institutionId, Class<T> responseType) throws TokenInvalidException {
        return hsbcErrorHandler.executeAndHandle(() -> super.fetchAccounts(httpClient, currentPath, accessToken, institutionId, responseType));

    }

    @Override
    public <T> T fetchTransactions(HttpClient httpClient, String currentPath, AccessMeans accessToken, String institutionId, Class<T> responseType) throws TokenInvalidException {
        return hsbcErrorHandler.executeAndHandle(() -> super.fetchTransactions(httpClient, currentPath, accessToken, institutionId, responseType));
    }

    @Override
    public <T> T fetchDirectDebits(HttpClient httpClient, String currentPath, AccessMeans accessToken, String institutionId, Class<T> responseType) throws TokenInvalidException {
        return hsbcErrorHandler.executeAndHandle(() -> super.fetchDirectDebits(httpClient, currentPath, accessToken, institutionId, responseType));
    }

    @Override
    public <T> T fetchStandingOrders(HttpClient httpClient, String currentPath, AccessMeans accessToken, String institutionId, Class<T> responseType) throws TokenInvalidException {
        return hsbcErrorHandler.executeAndHandle(() -> super.fetchStandingOrders(httpClient, currentPath, accessToken, institutionId, responseType));
    }

    @Override
    public <T> T fetchBalances(HttpClient httpClient, String currentPath, AccessMeans accessToken, String institutionId, Class<T> responseType) throws TokenInvalidException {
        return hsbcErrorHandler.executeAndHandle(() -> super.fetchBalances(httpClient, currentPath, accessToken, institutionId, responseType));
    }
}
