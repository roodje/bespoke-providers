package com.yolt.providers.stet.generic.service.fetchdata.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.error.DefaultFetchDataHttpErrorHandler;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;

@RequiredArgsConstructor
public class DefaultFetchDataRestClient implements FetchDataRestClient {

    protected final FetchDataHttpHeadersFactory headersFactory;
    protected final HttpErrorHandler errorHandler;

    public DefaultFetchDataRestClient(FetchDataHttpHeadersFactory headersFactory) {
        this.headersFactory = headersFactory;
        this.errorHandler = new DefaultFetchDataHttpErrorHandler();
    }

    @Override
    public StetAccountsResponseDTO getAccounts(HttpClient httpClient,
                                               String endpoint,
                                               DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_ACCOUNTS, StetAccountsResponseDTO.class);
    }

    @Override
    public ResponseEntity<Void> updateConsent(HttpClient httpClient,
                                              String endpoint,
                                              DataRequest dataRequest,
                                              Map<String, Object> body) throws TokenInvalidException {
        HttpMethod method = HttpMethod.PUT;
        String prometheusPath = "update_consent_for_accounts";
        HttpHeaders headers = headersFactory.createFetchDataHeaders(endpoint, dataRequest, method);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(endpoint, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchange(endpoint, method, entity, prometheusPath, Void.class), executionInfo);
    }

    @Override
    public StetBalancesResponseDTO getBalances(HttpClient httpClient,
                                               String endpoint,
                                               DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_BALANCES_BY_ACCOUNT_ID, StetBalancesResponseDTO.class);
    }

    @Override
    public StetTransactionsResponseDTO getTransactions(HttpClient httpClient,
                                                       String endpoint,
                                                       DataRequest dataRequest) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, GET_TRANSACTIONS_BY_ACCOUNT_ID, StetTransactionsResponseDTO.class);
    }

    protected <T> T getData(HttpClient httpClient, String endpoint, DataRequest dataRequest, String prometheusPath, Class<T> responseClass) throws TokenInvalidException {
        return getData(httpClient, endpoint, dataRequest, prometheusPath, responseClass, errorHandler);
    }

    protected <T> T getData(HttpClient httpClient, String endpoint, DataRequest dataRequest, String prometheusPath, Class<T> responseClass, HttpErrorHandler errorHandler) throws TokenInvalidException {
        HttpMethod method = HttpMethod.GET;
        HttpHeaders headers = headersFactory.createFetchDataHeaders(endpoint, dataRequest, method);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ExecutionInfo executionInfo = new ExecutionInfo(endpoint, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(endpoint, method, entity, prometheusPath, responseClass), executionInfo);
    }
}
