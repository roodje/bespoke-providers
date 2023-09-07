package com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import lombok.AllArgsConstructor;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

@AllArgsConstructor
public class DefaultClientAssertionProducer implements ClientAssertionProducer {
    private final UserRequestTokenSigner userRequestTokenSigner;
    private final String audience;

    @Override
    public String createNewClientRequestToken(final DefaultAuthMeans authenticationMeans,
                                              final Signer signer) throws TokenInvalidException {
        String clientId = authenticationMeans.getClientId();
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(clientId);
        claims.setSubject(clientId);
        claims.setExpirationTimeMinutesInTheFuture(1);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        claims.setAudience(audience);

        try {
            return userRequestTokenSigner.sign(authenticationMeans, claims, signer);
        } catch (JoseException e) {
            throw new TokenInvalidException("Error during signing token request.");
        }
    }
}