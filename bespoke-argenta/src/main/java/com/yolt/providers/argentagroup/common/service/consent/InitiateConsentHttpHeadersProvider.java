package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.common.service.CommonHttpHeadersProvider;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.dto.CreateConsentRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
public class InitiateConsentHttpHeadersProvider {

    private final CommonHttpHeadersProvider commonHttpHeadersProvider;

    public HttpHeaders provideHeaders(final UrlGetLoginRequest request,
                                      final DefaultAuthenticationMeans authenticationMeans,
                                      final CreateConsentRequest requestBody) {
        HttpHeaders headers = commonHttpHeadersProvider.provideCommonHeaders(
                authenticationMeans, request.getPsuIpAddress(), request.getSigner(), requestBody);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}
