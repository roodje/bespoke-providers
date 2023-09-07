package com.yolt.providers.stet.boursoramagroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserHashUtil {

    static final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setSkipAllValidators()
            .build();

    public static String fromAccessMeans(final String token) throws TokenInvalidException {
        final JwtClaims claims;
        try {
            claims = jwtConsumer.processToClaims(token);
        } catch (InvalidJwtException e) {
            throw new TokenInvalidException(e.getMessage());
        }
        final String userHash = (String) claims.getClaimValue("userHash");
        if (userHash == null || userHash.isEmpty()) {
            throw new TokenInvalidException("Claim \"userHash\" is missing in jwt token (AccessMeans) for user.  Shouldn't happen.");
        }
        return userHash;
    }
}