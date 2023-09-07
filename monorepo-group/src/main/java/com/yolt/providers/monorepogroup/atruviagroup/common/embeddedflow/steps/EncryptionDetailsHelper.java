package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.nimbusds.jose.jwk.RSAKey;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import lombok.experimental.UtilityClass;

import java.security.interfaces.RSAPublicKey;

@UtilityClass
class EncryptionDetailsHelper {
    static final String ALGORITHM = "RSA-OAEP-256";
    static final String ENCRYPTION_METHOD = "A256GCM";

    static EncryptionDetails createEncryptionDetails(RSAPublicKey aPublic) {
        var jwk = new RSAKey.Builder(aPublic).build();
        return EncryptionDetails.of(new EncryptionDetails.JWEDetails(null, ENCRYPTION_METHOD, null,
                new EncryptionDetails.RsaPublicJWK(ALGORITHM,
                        jwk.getKeyType().toString(),
                        jwk.getModulus().toString(), jwk
                        .getPublicExponent().toString())));
    }
}
