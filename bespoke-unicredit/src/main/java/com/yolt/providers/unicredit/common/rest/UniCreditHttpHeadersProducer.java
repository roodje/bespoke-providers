package com.yolt.providers.unicredit.common.rest;

import org.springframework.http.HttpHeaders;

public interface UniCreditHttpHeadersProducer {
    HttpHeaders createDefaultHeaders(String psuIpAddress);
    HttpHeaders createHeadersForConsent(final String psuIpAddress, final String redirectUrl, final PSUIDType psuidType);
    HttpHeaders createHeadersForData(final String psuIpAddress, final String consentId);
}
