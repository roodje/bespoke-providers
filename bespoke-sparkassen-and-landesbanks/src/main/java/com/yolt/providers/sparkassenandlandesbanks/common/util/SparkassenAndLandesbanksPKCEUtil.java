package com.yolt.providers.sparkassenandlandesbanks.common.util;

import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.experimental.UtilityClass;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@UtilityClass
public class SparkassenAndLandesbanksPKCEUtil {

    public OAuth2ProofKeyCodeExchange createRandomS256() {
        byte[] verifier = new byte[33];
        new SecureRandom().nextBytes(verifier);

        String codeVerifier = Base64.getUrlEncoder().encodeToString(verifier).replace("=", "");

        try {
            byte[] challenge = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes());
            String codeChallenge = Base64.getUrlEncoder().encodeToString(challenge).replace("=", "");
            String codeChallengeMethod = "S256";
            return new OAuth2ProofKeyCodeExchange(codeVerifier, codeChallenge, codeChallengeMethod);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate a SHA-256 challenge for PKCE.", e);
        }
    }
}
