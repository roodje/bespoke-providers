package com.yolt.providers.stet.generic.service.authorization.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class TokenRequest {
    private final String tokenUrl;
    private final DefaultAuthenticationMeans authMeans;
    private final Signer signer;
}
