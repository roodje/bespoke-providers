package com.yolt.providers.openbanking.ais.hsbcgroup.common.claims;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.JwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.jose4j.jwt.JwtClaims;

import java.util.LinkedHashMap;
import java.util.Map;

//acr sub, exp, iat
public class HsbcTokenClaimsProducerV2 extends DefaultTokenClaimsProducer {

    public HsbcTokenClaimsProducerV2(JwtClaimsProducer jwtClaimsProducer) {
        super(jwtClaimsProducer);
    }

    @Override
    public JwtClaims createUserRequestTokenClaims(DefaultAuthMeans authenticationMeans, String resourceId, String secretState, String redirectUrl, TokenScope scope, String... args) {
        JwtClaims claims = super.createUserRequestTokenClaims(authenticationMeans, resourceId, secretState, redirectUrl, scope, args);

        Map<String, Object> claimsObject = new LinkedHashMap<>();
        Map<String, Object> userInfo = new LinkedHashMap<>();
        Map<String, Object> idToken = new LinkedHashMap<>();
        Map<String, Object> openBankingIntentId = new LinkedHashMap<>();

        openBankingIntentId.put("value", resourceId);
        openBankingIntentId.put("essential", true);
        userInfo.put("openbanking_intent_id", openBankingIntentId);
        idToken.put("openbanking_intent_id", openBankingIntentId);
        Map<String, Object> acr = new LinkedHashMap<>();
        acr.put("essential", true);
        idToken.put("acr", acr);
        claimsObject.put("userinfo", userInfo);
        claimsObject.put("id_token", idToken);
        claims.setClaim("claims", claimsObject);

        claims.setSubject(authenticationMeans.getClientId());
        claims.setExpirationTimeMinutesInTheFuture(1);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        return claims;
    }
}
