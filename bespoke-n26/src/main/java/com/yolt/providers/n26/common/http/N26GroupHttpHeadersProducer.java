package com.yolt.providers.n26.common.http;

import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
class N26GroupHttpHeadersProducer {

    static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String CONSENT_ID_HEADER = "consent-id";

    HttpHeaders createConsentHeaders(N26GroupProviderState providerState) {
        HttpHeaders headers = createBasicHeaders();
        headers.setBearerAuth(providerState.getAccessToken());
        return headers;
    }

    HttpHeaders createAuthorizeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setAccept(singletonList(APPLICATION_JSON));
        return headers;
    }

    HttpHeaders createTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        return headers;
    }

    HttpHeaders createFetchDataHeaders(N26GroupProviderState providerState, String psuIpAddress) {
        HttpHeaders headers = createBasicHeaders();
        headers.set(CONSENT_ID_HEADER, providerState.getConsentId());
        headers.setBearerAuth(providerState.getAccessToken());
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.set(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        }
        return headers;
    }

    private HttpHeaders createBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
