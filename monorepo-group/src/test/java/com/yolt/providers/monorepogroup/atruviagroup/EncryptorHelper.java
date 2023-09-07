package com.yolt.providers.monorepogroup.atruviagroup;

import lombok.SneakyThrows;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

class EncryptorHelper {
    static final String ENCRYPTION = "A256GCM";
    static final String ALGORITHM = "RSA-OAEP-256";

    @SneakyThrows
    public static String encryptSensitiveFieldValueWithJose4j(String fieldValue, String publicKey) {
        var kf = KeyFactory.getInstance("RSA");
        var publicKeyBytes = Base64.getDecoder().decode(publicKey);
        var rsaPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        PublicJsonWebKey jwk = (PublicJsonWebKey) JsonWebKey.Factory.newJwk(rsaPublicKey);
        JsonWebEncryption senderJwe = new JsonWebEncryption();
        senderJwe.setPlaintext(fieldValue);
        senderJwe.setAlgorithmHeaderValue(ALGORITHM);
        senderJwe.setEncryptionMethodHeaderParameter(ENCRYPTION);
        senderJwe.setJwkHeader(jwk);
        senderJwe.setKey(jwk.getPublicKey());
        return senderJwe.getCompactSerialization();
    }
}
