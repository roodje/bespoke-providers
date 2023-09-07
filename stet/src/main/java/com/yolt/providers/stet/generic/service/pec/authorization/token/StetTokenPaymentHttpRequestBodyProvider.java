package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@RequiredArgsConstructor
public class StetTokenPaymentHttpRequestBodyProvider implements SepaTokenPaymentHttpRequestBodyProvider {

    protected final Scope paymentScope;

    @Override
    public HttpEntity<Map<String, ?>> createHttpEntity(MultiValueMap<String, String> requestBody, HttpHeaders headers) {
        return new HttpEntity<>(requestBody, headers);
    }

    @Override
    public MultiValueMap<String, String> createRequestBody(DefaultAuthenticationMeans authMeans) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        body.add(OAuth.SCOPE, paymentScope.getValue());
        return body;
    }
}
