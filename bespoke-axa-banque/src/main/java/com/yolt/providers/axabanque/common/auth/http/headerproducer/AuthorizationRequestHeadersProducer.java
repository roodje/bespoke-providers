package com.yolt.providers.axabanque.common.auth.http.headerproducer;

import org.springframework.http.HttpHeaders;

public interface AuthorizationRequestHeadersProducer {

    HttpHeaders createConsentCreationHeaders(String redirectUrl,
                                             String psuIpAddress,
                                             String xRequestId);

    HttpHeaders createAuthorizationResourceHeaders(String xRequestId);

    HttpHeaders createTokenHeaders();

    HttpHeaders getDeleteConsentHeaders(String xRequestId);
}
