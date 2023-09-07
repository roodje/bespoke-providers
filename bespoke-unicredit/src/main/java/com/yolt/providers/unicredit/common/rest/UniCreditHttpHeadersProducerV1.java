package com.yolt.providers.unicredit.common.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.Collections;

public class UniCreditHttpHeadersProducerV1 implements UniCreditHttpHeadersProducer {

    @Override
    public HttpHeaders createDefaultHeaders(String psuIpAddress) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (!StringUtils.isEmpty(psuIpAddress)) {
            httpHeaders.set("PSU-IP-Address", psuIpAddress);
        }
        return httpHeaders;
    }

    @Override
    public HttpHeaders createHeadersForConsent(final String psuIpAddress, final String redirectUrl, final PSUIDType psuidType) {
        HttpHeaders httpHeaders = createDefaultHeaders(psuIpAddress);
        httpHeaders.set("PSU-ID-Type", psuidType.name());
        httpHeaders.set("TPP-Redirect-Preferred", "true");
        httpHeaders.set("TPP-Redirect-URI", redirectUrl);
        return httpHeaders;
    }

    @Override
    public HttpHeaders createHeadersForData(final String psuIpAddress, final String consentId) {
        HttpHeaders httpHeaders = createDefaultHeaders(psuIpAddress);
        httpHeaders.add("Consent-ID", consentId);
        return httpHeaders;
    }

}
