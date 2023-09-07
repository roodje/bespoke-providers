package com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.RequiredArgsConstructor;
import org.jose4j.jwt.JwtClaims;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class KbcIeTokenClaimProducer implements TokenClaimsProducer {

    private final JwtClaimsProducer jwtClaimsProducer;

    @Override
    public JwtClaims createUserRequestTokenClaims(DefaultAuthMeans authenticationMeans, String resourceId, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = jwtClaimsProducer.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope);

        // Unfortunately, it seems that the library does not give much support to add custom serializers for claims.
        // See JwtClaims.toJson()
        // However, a nested Json object can be serialized by using nested maps..
        Map<String, Object> claimsObject = new LinkedHashMap<>();
        Map<String, Object> idToken = new LinkedHashMap<>();
        Map<String, Object> openBankingIntentId = new LinkedHashMap<>();
        openBankingIntentId.put("value", resourceId);
        openBankingIntentId.put("essential", true);
        idToken.put("openbanking_intent_id", openBankingIntentId);
        claimsObject.put("id_token", idToken);
        claims.setClaim("claims", claimsObject);
        return claims;
    }
}
