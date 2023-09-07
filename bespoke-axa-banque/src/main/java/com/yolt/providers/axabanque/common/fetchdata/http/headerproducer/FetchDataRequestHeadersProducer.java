package com.yolt.providers.axabanque.common.fetchdata.http.headerproducer;

import org.springframework.http.HttpHeaders;

public interface FetchDataRequestHeadersProducer {
    HttpHeaders getFetchBalancesHeaders(String accessToken, String consentId, String xRequestId, String psuIpAddress);

    HttpHeaders getAccountsHeaders(String accessToken, String consentId, String xRequestId, String psuIpAddress);

    HttpHeaders getTransactionsHeaders(String accessToken, String consentId, String xRequestId, String psuIpAddress);
}
