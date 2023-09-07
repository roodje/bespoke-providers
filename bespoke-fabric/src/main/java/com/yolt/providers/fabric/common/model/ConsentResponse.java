package com.yolt.providers.fabric.common.model;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;
import org.springframework.util.ObjectUtils;

@ProjectedPayload
public interface ConsentResponse {

    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$.consentStatus")
    String getConsentStatus();

    @JsonPath("$._links.scaRedirect.href")
    String getScaRedirect();

    default void validate() throws TokenInvalidException {
        if (ObjectUtils.isEmpty(getConsentId())) {
            throw new TokenInvalidException("Missing Consent Id");
        }
        if (ObjectUtils.isEmpty(getScaRedirect())) {
            throw new TokenInvalidException("Missing SCA status");
        }
        if (ObjectUtils.isEmpty(getConsentStatus())) {
            throw new TokenInvalidException("Missing Consent status");
        }
    }
}

