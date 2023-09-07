package com.yolt.providers.openbanking.ais.utils;

import lombok.experimental.UtilityClass;
import org.assertj.core.api.Fail;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;

@UtilityClass
public class JwtHelper {

    public String extractStringClaim(final JwtClaims jwtClaims, final String claimName) {
        try {
            return jwtClaims.getStringClaimValue(claimName);
        } catch (MalformedClaimException e) {
            return Fail.fail("Cannot extract " + claimName + " claim from JwtClaims");
        }
    }

    public JwtClaims parseJwtClaims(String requestJwt) {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setSkipSignatureVerification()
                .setDisableRequireSignature()
                .setSkipAllValidators()
                .build();
        try {
            JwtContext jwtContext = jwtConsumer.process(requestJwt);
            return jwtContext.getJwtClaims();
        } catch (InvalidJwtException e) {
            return Fail.fail("Cannot parse JWT");
        }
    }
}
