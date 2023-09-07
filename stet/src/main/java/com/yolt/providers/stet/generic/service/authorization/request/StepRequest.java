package com.yolt.providers.stet.generic.service.authorization.request;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Value;

@Value
public class StepRequest {

    private DefaultAuthenticationMeans authMeans;
    private String baseClientRedirectUrl;
    private String state;
    private String regionCode;
    private String consentLanguage;

    public static StepRequest baseStepRequest(DefaultAuthenticationMeans authMeans, String baseClientRedirectUrl, String state) {
        return new StepRequest(authMeans, baseClientRedirectUrl, state, null, null);
    }

    public static StepRequest regionAwareStepRequest(DefaultAuthenticationMeans authMeans, String baseClientRedirectUrl, String state, String regionCode) {
        return new StepRequest(authMeans, baseClientRedirectUrl, state, regionCode, null);
    }

    public static StepRequest consentLanguageAwareStepRequest(DefaultAuthenticationMeans authMeans, String baseClientRedirectUrl, String state, String consentLanguage) {
        return new StepRequest(authMeans, baseClientRedirectUrl, state, null, consentLanguage);
    }
}
