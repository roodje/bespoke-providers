package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeviceServerRequest {
    @JsonProperty("description")
    final String description;

    @JsonProperty("secret")
    final String oauthToken; // either the OAuth token or the bunq API key

    @JsonProperty("permitted_ips")
    final List<String> permittedIps;
}
