package com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

import static com.yolt.providers.common.constants.OAuth.*;

public class KbcIeGroupJwtClaimProducer implements JwtClaimsProducer {

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = new JwtClaims();

        claims.setClaim(RESPONSE_TYPE, "code");
        claims.setClaim(CLIENT_ID, authenticationMeans.getClientId());
        claims.setClaim(SCOPE, scope.getAuthorizationUrlScope());
        claims.setClaim(REDIRECT_URI, redirectUrl);
        return claims;
    }
}
