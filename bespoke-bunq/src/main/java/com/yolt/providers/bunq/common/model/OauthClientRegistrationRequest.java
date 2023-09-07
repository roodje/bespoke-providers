package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OauthClientRegistrationRequest {
    @JsonProperty("status")
    final String status;
}
