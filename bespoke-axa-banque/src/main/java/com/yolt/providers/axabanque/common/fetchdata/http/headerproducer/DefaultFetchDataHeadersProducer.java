package com.yolt.providers.axabanque.common.fetchdata.http.headerproducer;

import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.FetchDataRequestHeadersProducer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

public class DefaultFetchDataHeadersProducer implements FetchDataRequestHeadersProducer {

    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "PSU-IP-Address";

    @Override
    public HttpHeaders getFetchBalancesHeaders(String accessToken, String consentId, String xRequestId, String psuIpAddress) {
        return getAuthorizationHeadersWithTraceId(consentId, accessToken, xRequestId, psuIpAddress);
    }

    @Override
    public HttpHeaders getAccountsHeaders(String accessToken, String consentId, String xRequestId, String psuIpAddress) {
        return getAuthorizationHeadersWithTraceId(consentId, accessToken, xRequestId, psuIpAddress);
    }

    @Override
    public HttpHeaders getTransactionsHeaders(String accessToken, String consentId, String xRequestId, String psuIpAddress) {
        return getAuthorizationHeadersWithTraceId(consentId, accessToken, xRequestId, psuIpAddress);
    }

    private HttpHeaders getAuthorizationHeadersWithTraceId(String consentId, String accessToken, String xRequestId, String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isEmpty(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        headers.set(X_REQUEST_ID_HEADER, xRequestId);
        headers.set("consent-id", consentId);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
