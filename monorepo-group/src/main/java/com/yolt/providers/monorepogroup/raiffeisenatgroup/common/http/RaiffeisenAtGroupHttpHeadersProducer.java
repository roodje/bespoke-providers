package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import org.springframework.http.HttpHeaders;

public interface RaiffeisenAtGroupHttpHeadersProducer {
    HttpHeaders createClientCredentialTokenHttpHeaders();

    HttpHeaders createUserConsentHeaders(final String clientCredentialToken, final String redirectUri, final String psuIpAddress);

    HttpHeaders getUserConsentHeaders(final String clientCredentialToken, final String psuIpAddress);

    HttpHeaders deleteUserConsentHeaders(final String clientCredentialToken, final String psuIpAddress);

    HttpHeaders getFetchDataHeaders(final String clientAccessToken, final String consentId, final String psuIpAddress);

    HttpHeaders getRegistrationHeaders();
}
