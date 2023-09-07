package com.yolt.providers.ing.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.ing.common.auth.IngAuthData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestIngAuthData implements IngAuthData {

    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("cliend_id")
    private String clientId;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("refresh_token_expires_in")
    private Long refreshTokenExpiresIn;
    
    @JsonProperty("scope")
    private String scope;
}