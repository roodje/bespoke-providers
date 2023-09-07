package com.yolt.providers.axabanque.common.auth.http.headerproducer;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class DefaultAuthorizationRequestHeadersProducer implements AuthorizationRequestHeadersProducer {
    private static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    private static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";

    @Override
    public HttpHeaders createConsentCreationHeaders(String redirectUrl,
                                                    String psuIpAddress,
                                                    String xRequestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TPP_REDIRECT_URI_HEADER, redirectUrl);
        headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.set(X_REQUEST_ID_HEADER, xRequestId);
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Override
    public HttpHeaders createAuthorizationResourceHeaders(String xRequestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_REQUEST_ID_HEADER, xRequestId);
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Override
    public HttpHeaders createTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        return headers;
    }

    @Override
    public HttpHeaders getDeleteConsentHeaders(String xRequestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.set(X_REQUEST_ID_HEADER, xRequestId);
        return headers;
    }
}
