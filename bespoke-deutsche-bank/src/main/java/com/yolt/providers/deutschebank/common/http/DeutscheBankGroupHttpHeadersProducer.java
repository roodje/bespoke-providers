package com.yolt.providers.deutschebank.common.http;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class DeutscheBankGroupHttpHeadersProducer {

    static final String X_REQUEST_ID_HEADER = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final String CONSENT_ID_HEADER = "consent-id";
    private static final String PSU_ID_HEADER = "psu-id";
    private static final String PSU_ID_TYPE_HEADER = "psu-id-type";
    private static final String TPP_REDIRECT_URI_HEADER = "tpp-redirect-uri";
    private static final String TPP_NOK_REDIRECT_URI_HEADER = "tpp-nok-redirect-uri";

    private final DeutscheBankGroupProperties properties;

    public HttpHeaders createConsentHeaders(String psuIpAddress, String psuId, String redirectUri, String nokRedirectUri) {
        HttpHeaders headers = createBasicHeaders();
        headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        headers.add(PSU_ID_HEADER, psuId);
        headers.add(PSU_ID_TYPE_HEADER, properties.getPsuIdType());
        headers.add(TPP_REDIRECT_URI_HEADER, redirectUri);
        headers.add(TPP_NOK_REDIRECT_URI_HEADER, nokRedirectUri);
        return headers;
    }

    HttpHeaders createConsentStatusHeaders(String psuIpAddress) {
        HttpHeaders headers = createBasicHeaders();
        headers.add(PSU_IP_ADDRESS_HEADER, psuIpAddress);
        return headers;
    }

    HttpHeaders createFetchDataHeaders(DeutscheBankGroupProviderState providerState, String psuIpAddress) {
        HttpHeaders headers = createBasicHeaders();
        headers.set(CONSENT_ID_HEADER, providerState.getConsentId());
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
