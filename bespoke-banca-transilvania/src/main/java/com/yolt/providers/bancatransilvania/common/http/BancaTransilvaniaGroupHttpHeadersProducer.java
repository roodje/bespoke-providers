package com.yolt.providers.bancatransilvania.common.http;

import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class BancaTransilvaniaGroupHttpHeadersProducer {

    static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String CONSENT_ID_HEADER = "consent-id";

    HttpHeaders createRegistrationHeaders() {
        return createBasicHeaders();
    }

    HttpHeaders createConsentStatusHeaders(BancaTransilvaniaGroupProviderState providerState, String psuIpAddress) {
        HttpHeaders headers = createConsentHeaders(psuIpAddress);
        headers.setBearerAuth(providerState.getAccessToken());
        return headers;
    }

    HttpHeaders createConsentHeaders(String psuIpAddress) {
        HttpHeaders headers = createBasicHeaders();
        headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        return headers;
    }

    HttpHeaders createTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        return headers;
    }

    HttpHeaders createFetchDataHeaders(BancaTransilvaniaGroupProviderState providerState, String psuIpAddress) {
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
