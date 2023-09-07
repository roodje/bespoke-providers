package com.yolt.providers.stet.bpcegroup.common.service.pec;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentHttpRequestBodyProvider;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class BpceTokenPaymentHttpRequestBodyProvider extends StetTokenPaymentHttpRequestBodyProvider {
    public BpceTokenPaymentHttpRequestBodyProvider(Scope paymentScope) {
        super(paymentScope);
    }

    @Override
    public MultiValueMap<String, String> createRequestBody(DefaultAuthenticationMeans authMeans) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.CLIENT_ID, authMeans.getClientId());
        body.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        body.add(OAuth.SCOPE, paymentScope.getValue());
        return body;
    }
}
