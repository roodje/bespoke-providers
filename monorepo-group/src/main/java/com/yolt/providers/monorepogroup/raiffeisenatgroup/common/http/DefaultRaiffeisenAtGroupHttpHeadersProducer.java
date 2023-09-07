package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class DefaultRaiffeisenAtGroupHttpHeadersProducer implements RaiffeisenAtGroupHttpHeadersProducer {
    @Override
    public HttpHeaders createClientCredentialTokenHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return headers;
    }

    @Override
    public HttpHeaders createUserConsentHeaders(final String clientCredentialToken, final String redirectUri, final String psuIpAddress) {
        HttpHeaders headers = createCommonHttpHeaders(clientCredentialToken, psuIpAddress);
        headers.set("TPP-Redirect-URI", redirectUri);
        return headers;

    }

    @Override
    public HttpHeaders getUserConsentHeaders(final String clientCredentialToken, final String psuIpAddress) {
        return createCommonHttpHeaders(clientCredentialToken, psuIpAddress);
    }

    @Override
    public HttpHeaders deleteUserConsentHeaders(final String clientCredentialToken, final String psuIpAddress) {
        return createCommonHttpHeaders(clientCredentialToken, psuIpAddress);
    }

    @Override
    public HttpHeaders getFetchDataHeaders(final String clientAccessToken, final String consentId, final String psuIpAddress) {
        HttpHeaders headers = createCommonHttpHeaders(clientAccessToken, psuIpAddress);
        headers.set("Consent-ID", consentId);
        return headers;
    }

    @Override
    public HttpHeaders getRegistrationHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders createCommonHttpHeaders(final String clientCredentialToken, final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientCredentialToken);
        if (StringUtils.isNotEmpty(psuIpAddress)) {
            headers.add("PSU-IP-Address", psuIpAddress);
        }
        return headers;
    }
}
