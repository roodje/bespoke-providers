package com.yolt.providers.rabobank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenResponseDTO {

    public AccessTokenResponseDTO(String accessToken,
                                  String refreshToken,
                                  int expiresIn,
                                  int refreshTokenExpiresIn,
                                  String tokenType,
                                  String scope) {
        this(accessToken, refreshToken, expiresIn, refreshTokenExpiresIn, tokenType, scope, null);
    }

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token_expires_in")
    private int refreshTokenExpiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;

    private String metadata;
}
