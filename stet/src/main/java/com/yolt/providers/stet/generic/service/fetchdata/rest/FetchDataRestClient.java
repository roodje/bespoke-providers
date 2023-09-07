package com.yolt.providers.stet.generic.service.fetchdata.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface FetchDataRestClient {

    StetAccountsResponseDTO getAccounts(HttpClient httpClient,
                                        String endpoint,
                                        DataRequest dataRequest) throws TokenInvalidException;

    ResponseEntity<Void> updateConsent(HttpClient httpClient,
                                       String endpoint,
                                       DataRequest dataRequest,
                                       Map<String, Object> body) throws TokenInvalidException;

    StetBalancesResponseDTO getBalances(HttpClient httpClient,
                                        String endpoint,
                                        DataRequest dataRequest) throws TokenInvalidException;

    StetTransactionsResponseDTO getTransactions(HttpClient httpClient,
                                                String endpoint,
                                                DataRequest dataRequest) throws TokenInvalidException;
}
