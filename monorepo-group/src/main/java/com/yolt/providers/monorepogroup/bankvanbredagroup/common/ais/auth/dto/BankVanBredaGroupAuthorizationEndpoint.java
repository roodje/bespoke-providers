package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface BankVanBredaGroupAuthorizationEndpoint {
    @JsonPath("$.authorization_endpoint")
    String getAuthorizationUrl();
}
