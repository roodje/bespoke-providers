package com.yolt.providers.bancatransilvania.common.domain.model.registration;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface RegistrationResponse {

    @JsonPath("$.client_id")
    String getClientId();

    @JsonPath("$.client_secret")
    String getClientSecret();
}
