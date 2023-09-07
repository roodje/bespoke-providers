package com.yolt.providers.openbanking.ais.vanquisgroup.common.claims;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

public class VanquisGroupTokenClaimsProducerV2 extends DefaultTokenClaimsProducer {

    public VanquisGroupTokenClaimsProducerV2(JwtClaimsProducer jwtClaimsProducer) {
        super(jwtClaimsProducer);
    }

    @Override
    public JwtClaims createUserRequestTokenClaims(DefaultAuthMeans authenticationMeans, String resourceId, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = super.createUserRequestTokenClaims(authenticationMeans, resourceId, secretState, redirectUrl, scope, args);
        claims.setSubject(authenticationMeans.getClientId());
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        return claims;
    }
}
