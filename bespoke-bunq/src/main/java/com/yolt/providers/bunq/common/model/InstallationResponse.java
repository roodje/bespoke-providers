package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface InstallationResponse {

    @JsonPath("$.Response.[?(@.Token)].Token")
    Token getToken();

    @JsonPath("$.Response.[?(@.ServerPublicKey)].ServerPublicKey")
    ServerPublicKey getServerPublicKey();

    interface ServerPublicKey {
        @JsonPath("$.server_public_key")
        String getPublicKey();
    }
}
