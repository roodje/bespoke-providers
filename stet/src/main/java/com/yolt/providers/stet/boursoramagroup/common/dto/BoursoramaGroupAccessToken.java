package com.yolt.providers.stet.boursoramagroup.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BoursoramaGroupAccessToken {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
    private boolean refreshed;
}