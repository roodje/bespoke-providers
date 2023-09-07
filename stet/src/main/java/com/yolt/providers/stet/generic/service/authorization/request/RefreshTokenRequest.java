package com.yolt.providers.stet.generic.service.authorization.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenRequest extends TokenRequest {
    private final String refreshToken;
    private final Scope refreshTokenScope;

    public RefreshTokenRequest(String tokenUrl, DefaultAuthenticationMeans authMeans, String refreshToken, Scope refreshTokenScope, Signer signer) {
        super(tokenUrl, authMeans, signer);
        this.refreshToken = refreshToken;
        this.refreshTokenScope = refreshTokenScope;
    }
}
