package com.yolt.providers.openbanking.ais.rbsgroup.coutts.service.claims;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.AllArgsConstructor;
import org.jose4j.jwt.JwtClaims;

import static com.yolt.providers.common.constants.OAuth.MAX_AGE;

@AllArgsConstructor
public class CouttsJwtClaimsProducerDecorator implements JwtClaimsProducer {

    private final DefaultJwtClaimsProducer wrapee;
    private final int expirationTimeInMinutes;
    private final String jwtId;

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = wrapee.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        claims.unsetClaim(MAX_AGE);
        claims.setExpirationTimeMinutesInTheFuture(expirationTimeInMinutes);
        claims.setIssuedAtToNow();
        claims.setJwtId(jwtId);
        claims.setSubject(authenticationMeans.getClientId());
        return claims;
    }
}
