package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

/**
 * FAPI-compliant JwtClaimsProducer
 */
public class LloydsBankingGroupJwtClaimsProducerV3 implements JwtClaimsProducer {

    private final JwtClaimsProducer wrappee;
    private final LLoydsBankingGroupNonceProvider nonceProvider;

    //use FapiCompilant as constructor parameter
    public LloydsBankingGroupJwtClaimsProducerV3(JwtClaimsProducer claimsProducer, final LLoydsBankingGroupNonceProvider nonceProvider) {
        this.wrappee = claimsProducer;
        this.nonceProvider = nonceProvider;
    }

    @Override
    public JwtClaims createJwtClaims(DefaultAuthMeans authenticationMeans, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = wrappee.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        try {
            claims.setClaim(OAuth.NONCE, nonceProvider.prepareNonce(claims.getStringClaimValue(OAuth.NONCE)));
            return claims;
        } catch (MalformedClaimException e) {
            return claims;
        }
    }
}
