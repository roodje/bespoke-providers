package com.yolt.providers.stet.generic.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Token {

    private String accessToken;
    private String clientId;
    private long expiresIn;
    private String refreshToken;
    private String tokenType;
    private String userId;
    private String scope;
}
