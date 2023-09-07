package com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer;

import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import org.springframework.util.MultiValueMap;

public interface StarlingBankAuthorizationUrlParametersProducer {

    MultiValueMap<String, String> createAuthorizationUrlParameters(String redirectUrl, String loginState, StarlingBankAuthenticationMeans authMeans);
}
