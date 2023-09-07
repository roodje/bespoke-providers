package com.yolt.providers.volksbank.common.pis.pec;

import com.yolt.providers.volksbank.common.util.HttpUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class VolksbankPisHttpHeadersFactory {

    private static final String PSU_IP_ADDRESS_HEADER = "PSU-IP-Address";

    public HttpHeaders createPaymentInitiationHttpHeaders(String clientId, String psuIpAddress) {
        var httpHeaders = createCommonHttpHeaders(clientId);
        httpHeaders.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        return httpHeaders;
    }

    public HttpHeaders createCommonHttpHeaders(String clientId) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, clientId);
        return httpHeaders;
    }

    public HttpHeaders createAccessTokenHttpHeaders(String clientId, String clientSecret) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, HttpUtils.basicCredentials(clientId, clientSecret));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }
}
