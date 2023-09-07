package com.yolt.providers.openbanking.ais.capitalonegroup;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import org.bouncycastle.util.encoders.Base64;

public class CapitalOneGroupJwsSigningResult implements JwsSigningResult {

    private static final String ENCODED_SIGNATURE = "encodedsignature";

    @Override
    public String getCompactSerialization() {
        return Base64.toBase64String(ENCODED_SIGNATURE.getBytes());
    }

    @Override
    public String getDetachedContentCompactSerialization() {
        return null;
    }
}
