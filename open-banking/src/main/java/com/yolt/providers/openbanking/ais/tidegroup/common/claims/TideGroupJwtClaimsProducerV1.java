package com.yolt.providers.openbanking.ais.tidegroup.common.claims;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

import java.util.function.Function;

public class TideGroupJwtClaimsProducerV1 extends DefaultJwtClaimsProducer {

    public TideGroupJwtClaimsProducerV1(Function<DefaultAuthMeans, String> issuerSupplier,
                                        String audience) {
        super(issuerSupplier, audience);
    }

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans,
                                     String secretState,
                                     String redirectUrl,
                                     TokenScope scope,
                                     String... args) {
        JwtClaims claims = super.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        claims.setExpirationTimeMinutesInTheFuture(1);
        return claims;
    }
}
