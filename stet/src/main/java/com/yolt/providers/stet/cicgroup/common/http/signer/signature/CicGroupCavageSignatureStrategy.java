package com.yolt.providers.stet.cicgroup.common.http.signer.signature;

import com.yolt.providers.stet.generic.http.signer.signature.CavageSignatureStrategy;
import com.yolt.securityutils.signing.SignatureAlgorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.PSU_IP_ADDRESS;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.HttpHeaders.HOST;

public class CicGroupCavageSignatureStrategy extends CavageSignatureStrategy {

    public CicGroupCavageSignatureStrategy(SignatureAlgorithm signatureAlgorithm) {
        super(signatureAlgorithm);
    }

    @Override
    public List<String> getHeadersToSign() {
        return new LinkedList<>(Arrays.asList(HOST, DATE, CONTENT_TYPE, X_REQUEST_ID, DIGEST, PSU_IP_ADDRESS));
    }
}
