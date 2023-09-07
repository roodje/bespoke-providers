package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface SepaTokenPaymentHttpRequestBodyProvider {

    HttpEntity<Map<String, ?>> createHttpEntity(MultiValueMap<String, String> requestBody, HttpHeaders headers);

    MultiValueMap<String, String> createRequestBody(DefaultAuthenticationMeans authMeans);
}
