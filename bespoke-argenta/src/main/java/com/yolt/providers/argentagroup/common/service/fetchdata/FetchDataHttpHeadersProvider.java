package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.CommonHttpHeadersProvider;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class FetchDataHttpHeadersProvider {

    private static final String CONSENT_ID_HEADER_NAME = "consent-id";
    private static final byte[] EMPTY_REQUEST_BODY = new byte[0];

    private final CommonHttpHeadersProvider commonHttpHeadersProvider;

    public HttpHeaders providerHeaders(final UrlFetchDataRequest request,
                                       final DefaultAuthenticationMeans authenticationMeans,
                                       final AccessMeans accessMeans) {
        HttpHeaders headers = commonHttpHeadersProvider.provideCommonHeaders(
                authenticationMeans, request.getPsuIpAddress(), request.getSigner(), EMPTY_REQUEST_BODY);

        headers.setBearerAuth(accessMeans.getAccessToken());
        headers.set(CONSENT_ID_HEADER_NAME, accessMeans.getConsentId());

        return headers;
    }
}
