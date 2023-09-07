package com.yolt.providers.stet.boursoramagroup.common.service.pec.authorization.token;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentHttpRequestBodyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@RequiredArgsConstructor
public class BoursoramaGroupTokenPaymentHttpRequestBodyProvider implements SepaTokenPaymentHttpRequestBodyProvider {

    private final Scope paymentScope;

    @Override
    public HttpEntity<Map<String, ?>> createHttpEntity(MultiValueMap<String, String> requestBody, HttpHeaders headers) {
        return new HttpEntity<>(requestBody.toSingleValueMap(), headers);
    }

    @Override
    public MultiValueMap<String, String> createRequestBody(DefaultAuthenticationMeans authMeans) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        requestBody.add(OAuth.SCOPE, paymentScope.getValue());
        requestBody.add(OAuth.CLIENT_ID, authMeans.getClientId());
        return requestBody;
    }
}
