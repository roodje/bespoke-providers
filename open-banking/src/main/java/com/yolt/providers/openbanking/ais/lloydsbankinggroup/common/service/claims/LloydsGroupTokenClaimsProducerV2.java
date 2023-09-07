package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.AllArgsConstructor;
import org.jose4j.jwt.JwtClaims;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class LloydsGroupTokenClaimsProducerV2 implements TokenClaimsProducer {

    private final JwtClaimsProducer jwtClaimsProducer;

    @Override
    public JwtClaims createUserRequestTokenClaims(DefaultAuthMeans authenticationMeans, String resourceId, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = jwtClaimsProducer.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);
        Map<String, Object> claimsObject = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        Map<String, Object> idToken = new HashMap<>();
        Map<String, Object> openBankingIntentId = new HashMap<>();
        Map<String, Object> acr = new HashMap<>();
        acr.put("essential", true);
        openBankingIntentId.put("value", resourceId);
        openBankingIntentId.put("essential", true);
        userInfo.put("openbanking_intent_id", openBankingIntentId);
        idToken.put("openbanking_intent_id", openBankingIntentId);
        idToken.put("acr", acr);
        claimsObject.put("userinfo", userInfo);
        claimsObject.put("id_token", idToken);
        claims.setClaim("claims", claimsObject);
        return claims;
    }
}
