package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Psd2ProviderResponse {
    @JsonPath("$.Response.[?(@.CredentialPasswordIp)].CredentialPasswordIp")
    CredentialPasswordIp getCredentialPasswordIp();
}
