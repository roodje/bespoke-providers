package com.yolt.providers.monorepogroup.olbgroup.common.http;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class OlbGroupHttpHeadersProducer {

    static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String PSU_ID_HEADER = "psu-id";
    private static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    private static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    private static final String CONSENT_ID_HEADER = "consent-id";

    public HttpHeaders createConsentHeaders(String psuIpAddress, String psuId, String redirectUri) {
        HttpHeaders headers = createBasicHeaders();
        headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.add(PSU_ID_HEADER, psuId);
        headers.add(TPP_REDIRECT_URI_HEADER, redirectUri);
        headers.add(TPP_REDIRECT_PREFERRED_HEADER, "true");
        return headers;
    }

    private HttpHeaders createBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    public HttpHeaders createFetchDataHeaders(String consentId, String psuIpAddress) {
        HttpHeaders headers = createBasicHeaders();
        headers.set(CONSENT_ID_HEADER, consentId);
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        return headers;
    }
}
