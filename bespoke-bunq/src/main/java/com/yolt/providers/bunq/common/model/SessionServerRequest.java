package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SessionServerRequest {
    @JsonProperty("secret")
    final String oauthToken; // either the OAuth token or the com.yolt.providers.bunq API key
}
