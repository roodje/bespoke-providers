package com.yolt.providers.openbanking.ais.generic2.claims.producer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

public interface JwtClaimsProducer {
    JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args);
}
