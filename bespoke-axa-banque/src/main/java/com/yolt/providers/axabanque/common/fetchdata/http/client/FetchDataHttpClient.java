package com.yolt.providers.axabanque.common.fetchdata.http.client;

import com.yolt.providers.axabanque.common.model.external.Accounts;
import com.yolt.providers.axabanque.common.model.external.Balances;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.common.exception.TokenInvalidException;

import java.time.Instant;

public interface FetchDataHttpClient {
    Accounts getAccounts(String accessToken, String consentId, String xRequestId, String psuIpAddress) throws TokenInvalidException;

    Balances getBalances(String accessToken, String consentId, String accountId, String xRequestId, String psuIpAddress) throws TokenInvalidException;

    Transactions getTransactions(String accountId, Instant fetchDataStartTime, String accessToken, String consentId, String uri, String xRequestId, int pageNumber, String psuIpAddress) throws TokenInvalidException;

    String getTransactionsEndpoint();
}
