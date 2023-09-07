package com.yolt.providers.stet.bnpparibasgroup.common.pec;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentHttpRequestBodyProvider;
import org.springframework.util.MultiValueMap;

public class BnpParibasGroupTokenPaymentHttpRequestBodyProvider extends StetTokenPaymentHttpRequestBodyProvider {

    public BnpParibasGroupTokenPaymentHttpRequestBodyProvider(Scope paymentScope) {
        super(paymentScope);
    }

    @Override
    public MultiValueMap<String, String> createRequestBody(DefaultAuthenticationMeans authMeans) {
        MultiValueMap<String, String> requestBody = super.createRequestBody(authMeans);
        requestBody.add("client_id", authMeans.getClientId());
        return requestBody;
    }
}
