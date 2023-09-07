package com.yolt.providers.yoltprovider;

import com.nimbusds.jose.jwk.RSAKey;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import lombok.experimental.UtilityClass;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.apache.commons.lang3.tuple.Pair;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.lang.JoseException;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public class FormEncryption {

    /**
     * Returns an encryptionDetails object (public, for the client) and a base64 encoded private key.
     * The private key can be provided to {@link #decryptValues(FilledInUserSiteFormValues, String)} to decrypt the values provided by the user.
     */
    Pair<EncryptionDetails, String> createEncryptionDetails() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        var jwk = new RSAKey.Builder((RSAPublicKey) pair.getPublic()).build();
        var jweDetails = new EncryptionDetails.JWEDetails(null, "A256GCM", null,
                new EncryptionDetails.RsaPublicJWK(
                        "RSA-OAEP-256",
                        jwk.getKeyType().toString(),
                        jwk.getModulus().toString(),
                        jwk.getPublicExponent().toString())
        );
        return Pair.of(EncryptionDetails.of(jweDetails), Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
    }

    FilledInUserSiteFormValues decryptValues(FilledInUserSiteFormValues values, String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, JoseException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        var key = kf.generatePrivate(spec);


        FilledInUserSiteFormValues decodedValues = new FilledInUserSiteFormValues();

        for (String fieldId : values.getValueMap().keySet()) {
            String value = values.get(fieldId);
            JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setKey(key);
            jwe.setCompactSerialization(value);
            decodedValues.add(fieldId, jwe.getPlaintextString());
        }

        return decodedValues;
    }
}
