package com.yolt.providers.fabric.common.model;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.util.ObjectUtils;

@ProjectedPayload
public interface AuthorizationConsentResourceResponse {
    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$.scaStatus")
    String getScaStatus();

    default void validate() throws TokenInvalidException {
        if (ObjectUtils.isEmpty(getConsentId())) {
            throw new TokenInvalidException("Missing Consent Id");
        }
        if (ObjectUtils.isEmpty(getScaStatus())) {
            throw new TokenInvalidException("Missing SCA status");
        }
    }
}
