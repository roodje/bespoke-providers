package com.yolt.providers.knabgroup.knab;

import com.yolt.providers.knabgroup.common.dto.external.AuthData;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@Setter
@NoArgsConstructor
public class TokenResponseMock implements AuthData {

    private String accessToken = "clientToken";
    private long expiresIn = 3600;
    private String tokenType = "tokenType";
    private String refreshToken = "refreshToken";
    private String scope = "psd2 offline_access";

    public TokenResponseMock(String access, String scope) {
        this.accessToken = access + "AccessToken";
        this.refreshToken = access + "RefreshToken";
        this.scope += scope != null ? " AIS:" + access + "ConsentId" : "";
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String getTokenType() {
        return tokenType;
    }

    @Override
    public Long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String getScope() {
        return scope;
    }
}