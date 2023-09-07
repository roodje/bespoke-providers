package com.yolt.providers.openbanking.ais.generic2.claims.producer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.AllArgsConstructor;
import org.jose4j.jwt.JwtClaims;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.yolt.providers.common.constants.OAuth.*;

@AllArgsConstructor
public class DefaultJwtClaimsProducer implements JwtClaimsProducer {

    private final Function<DefaultAuthMeans, String> issuerSupplier;
    private final String audience;

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans,
                                     String secretState,
                                     String redirectUrl,
                                     TokenScope scope,
                                     String... args) {
        JwtClaims claims = new JwtClaims();

        claims.setIssuer(issuerSupplier.apply(authenticationMeans));
        claims.setAudience(audience);

        claims.setClaim(RESPONSE_TYPE, "code id_token");
        claims.setClaim(CLIENT_ID, authenticationMeans.getClientId());
        claims.setClaim(REDIRECT_URI, redirectUrl);
        claims.setClaim(SCOPE, scope.getAuthorizationUrlScope());
        claims.setClaim(STATE, secretState);
        claims.setClaim(NONCE, secretState);
        claims.setClaim(MAX_AGE, TimeUnit.DAYS.toSeconds(1));
        return claims;
    }
}