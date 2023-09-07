package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Content of the body of a Select PSU Authentication Method Request.
 */
@Data
public class SelectPsuAuthenticationMethod {

    @JsonProperty("authenticationMethodId")
    private String authenticationMethodId;

    public SelectPsuAuthenticationMethod authenticationMethodId(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
        return this;
    }
}

