package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Authentication Object.
 */
@Data
public class AuthenticationObject {

    @JsonProperty("authenticationType")
    private AuthenticationType authenticationType;

    @JsonProperty("authenticationVersion")
    private String authenticationVersion;

    @JsonProperty("authenticationMethodId")
    private String authenticationMethodId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("explanation")
    private String explanation;
}

