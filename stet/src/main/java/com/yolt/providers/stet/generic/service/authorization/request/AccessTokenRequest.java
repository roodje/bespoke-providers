package com.yolt.providers.stet.generic.service.authorization.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Scope;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class AccessTokenRequest extends TokenRequest {
    private final String authorizationCode;
    private final String redirectUrl;
    private final DataProviderState providerState;
    private final Scope accessTokenScope;

    public AccessTokenRequest(String tokenUrl,
                              DefaultAuthenticationMeans authMeans,
                              String authorizationCode,
                              String redirectUrl,
                              DataProviderState providerState,
                              Scope accessTokenScope,
                              Signer signer) {
        super(tokenUrl, authMeans, signer);
        this.authorizationCode = authorizationCode;
        this.redirectUrl = redirectUrl;
        this.providerState = providerState;
        this.accessTokenScope = accessTokenScope;
    }
}
