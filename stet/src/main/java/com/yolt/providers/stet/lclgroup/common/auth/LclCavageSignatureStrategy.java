package com.yolt.providers.stet.lclgroup.common.auth;

import com.yolt.providers.stet.generic.http.signer.signature.CavageSignatureStrategy;
import com.yolt.securityutils.signing.SignatureAlgorithm;

import java.util.List;

import static org.springframework.http.HttpHeaders.DATE;

public class LclCavageSignatureStrategy extends CavageSignatureStrategy {

    public LclCavageSignatureStrategy(final SignatureAlgorithm signatureAlgorithm) {
        super(signatureAlgorithm);
    }

    @Override
    public List<String> getHeadersToSign() {
        return List.of(DATE);
    }
}
