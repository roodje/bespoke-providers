package com.yolt.providers.stet.generic.service.fetchdata.transaction;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;

import java.time.Instant;
import java.util.List;

public interface FetchTransactionsStrategy {

    List<StetTransactionDTO> fetchTransactions(HttpClient httpClient,
                                               String endpoint,
                                               DataRequest dataRequest,
                                               Instant fetchStartTime) throws TokenInvalidException;
}
