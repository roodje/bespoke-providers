package com.yolt.providers.triodosbank.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationResponse {

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("client_id")
    private String clientId;
}
