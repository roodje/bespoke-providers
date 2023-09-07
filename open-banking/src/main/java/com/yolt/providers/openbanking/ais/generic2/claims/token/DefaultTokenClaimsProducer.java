package com.yolt.providers.openbanking.ais.generic2.claims.token;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.AllArgsConstructor;
import org.jose4j.jwt.JwtClaims;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@AllArgsConstructor
public class DefaultTokenClaimsProducer implements TokenClaimsProducer {
    private final JwtClaimsProducer jwtClaimsProducer;

    @Override
    public JwtClaims createUserRequestTokenClaims(DefaultAuthMeans authenticationMeans, String resourceId, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = jwtClaimsProducer.createJwtClaims(authenticationMeans, secretState, redirectUrl, scope, args);

        // Unfortunately, it seems that the library does not give much support to add custom serializers for claims.
        // See JwtClaims.toJson()
        // However, a nested Json object can be serialized by using nested maps..
        Map<String, Object> claimsObject = new LinkedHashMap<>();
        Map<String, Object> userInfo = new LinkedHashMap<>();
        Map<String, Object> idToken = new LinkedHashMap<>();
        Map<String, Object> openBankingIntentId = new LinkedHashMap<>();
        Map<String, Object> acr = new LinkedHashMap<>();
        acr.put("essential", true);
        acr.put("values", Arrays.asList("urn:openbanking:psd2:sca", "urn:openbanking:psd2:ca"));
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
