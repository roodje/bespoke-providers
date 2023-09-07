package com.yolt.providers.stet.bpcegroup.common.onboarding;

import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import lombok.Getter;

@Getter
class BpceGroupRegistrationRequest extends RegistrationRequest {

    private final String registrationAccessToken;

    public BpceGroupRegistrationRequest(String registrationAccessToken, RegistrationRequest registrationRequest) {
        super(registrationRequest.getAuthMeans(), registrationRequest.getSigner(), registrationRequest.getLastExternalTraceIdSupplier(), registrationRequest.getRedirectUrl(), registrationRequest.getProviderIdentifier());
        this.registrationAccessToken = registrationAccessToken;
    }
}
