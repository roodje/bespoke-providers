package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
public class HandelsbankenGroupHttpHeadersProducerV1 implements HandelsbankenGroupHttpHeadersProducer {

    private static final String TPP_TRANSACTION_ID_HEADER_NAME = "TPP-Transaction-ID";
    private static final String TPP_REQUEST_ID_HEADER_NAME = "TPP-Request-ID";
    private static final String X_IBM_CLIENT_ID_HEADER_NAME = "X-IBM-Client-Id";
    private static final String COUNTRY_HEADER_NAME = "Country";

    private final String countryCode;

    @Override
    public HttpHeaders thirdPartyHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.add(TPP_REQUEST_ID_HEADER_NAME, ExternalTracingUtil.createLastExternalTraceId());
        httpHeaders.add(TPP_TRANSACTION_ID_HEADER_NAME, UUID.randomUUID().toString());
        return httpHeaders;
    }

    @Override
    public HttpHeaders tokenHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

    @Override
    public HttpHeaders registrationAndSubscriptionHeaders(String tppId, String ccgToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setBearerAuth(ccgToken);
        httpHeaders.add(X_IBM_CLIENT_ID_HEADER_NAME, tppId);
        httpHeaders.add(TPP_REQUEST_ID_HEADER_NAME, ExternalTracingUtil.createLastExternalTraceId());
        httpHeaders.add(TPP_TRANSACTION_ID_HEADER_NAME, UUID.randomUUID().toString());
        return httpHeaders;
    }

    @Override
    public HttpHeaders createConsentHeaders(String clientId, String ccgToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setBearerAuth(ccgToken);
        httpHeaders.add(X_IBM_CLIENT_ID_HEADER_NAME, clientId);
        httpHeaders.add(TPP_REQUEST_ID_HEADER_NAME, ExternalTracingUtil.createLastExternalTraceId());
        httpHeaders.add(TPP_TRANSACTION_ID_HEADER_NAME, UUID.randomUUID().toString());
        httpHeaders.add(COUNTRY_HEADER_NAME, countryCode);
        return httpHeaders;
    }
}
