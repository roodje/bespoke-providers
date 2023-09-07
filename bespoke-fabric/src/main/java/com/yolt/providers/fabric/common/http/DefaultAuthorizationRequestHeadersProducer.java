package com.yolt.providers.fabric.common.http;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import org.springframework.http.HttpHeaders;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class DefaultAuthorizationRequestHeadersProducer {
    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "TPP-Redirect-Preferred";
    private static final String PSU_IP_ADDRESS_HEADER = "PSU-IP-Address";
    private static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    private static final String CONSENT_ID_HEADER = "Consent-ID";

    public HttpHeaders createConsentAndAuthorizationCreationHeaders(final String redirectUrl,
                                                                    final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TPP_REDIRECT_PREFERRED_HEADER, "true");
        headers.set(TPP_REDIRECT_URI_HEADER, redirectUrl);
        headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    public HttpHeaders createFetchDataHeaders(final String consentId,
                                              final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONSENT_ID_HEADER, consentId);
        headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        return headers;
    }

    public HttpHeaders createConsentDeletionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId());
        return headers;
    }
}
