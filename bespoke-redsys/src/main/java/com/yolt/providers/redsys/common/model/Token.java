package com.yolt.providers.redsys.common.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * The response for token is not presented in Redsys documentation https://market.apis-i.redsys.es/psd2/xs2a/nodos/oauth2-tokengen
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Token {

    private String accessToken;

    private String refreshToken;

    private long expiresIn;

    private String tokenType;

}
