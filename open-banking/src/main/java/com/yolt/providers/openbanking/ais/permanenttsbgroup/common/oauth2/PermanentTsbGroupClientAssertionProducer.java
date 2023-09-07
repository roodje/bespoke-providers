package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.util.UUID;

public class PermanentTsbGroupClientAssertionProducer extends DefaultClientAssertionProducer {

    private final UserRequestTokenSigner userRequestTokenSigner;
    private final String audience;

    public PermanentTsbGroupClientAssertionProducer(UserRequestTokenSigner userRequestTokenSigner,
                                                    String audience) {
        super(userRequestTokenSigner, audience);
        this.userRequestTokenSigner = userRequestTokenSigner;
        this.audience = audience;
    }

    @Override
    public String createNewClientRequestToken(final DefaultAuthMeans authenticationMeans,
                                              final Signer signer) throws TokenInvalidException {
        String clientId = authenticationMeans.getClientId();
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(clientId);
        claims.setSubject(clientId);
        claims.setExpirationTimeMinutesInTheFuture(1);
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setAudience(audience);

        try {
            return userRequestTokenSigner.sign(authenticationMeans, claims, signer);
        } catch (JoseException e) {
            throw new TokenInvalidException("Error during signing token request.");
        }
    }
}