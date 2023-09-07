package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestTokenResponseDTO implements TokenResponseDTO {

    private String accessToken;
    private String clientId;
    private long expiresIn;
    private String refreshToken;
    private String tokenType;
    private String userId;
    private String scope;
}
