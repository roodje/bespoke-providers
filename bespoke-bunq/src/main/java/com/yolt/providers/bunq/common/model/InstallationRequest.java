package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstallationRequest {
    @JsonProperty("client_public_key")
    final String clientPublicKey;
}
