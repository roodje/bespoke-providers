package com.yolt.providers.amexgroup.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;
    private String refreshToken;
    private String macKey;
    private String macAlgorithm;
}
