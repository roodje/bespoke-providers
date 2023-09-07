package com.yolt.providers.stet.cmarkeagroup.common.http;

import com.yolt.providers.stet.generic.http.signer.signature.CavageSignatureStrategy;
import com.yolt.securityutils.signing.SignatureAlgorithm;

import java.util.Arrays;
import java.util.List;

public class CmArkeaGroupSignatureStrategy extends CavageSignatureStrategy {

    private static final String HEADER_DIGEST = "digest";
    private static final String HEADER_X_REQUEST_ID = "x-request-id";
    private static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    private static final List<String> PSU_HEADERS_TO_SIGN = Arrays.asList(HEADER_X_REQUEST_ID, HEADER_DIGEST, PSU_IP_ADDRESS_HEADER);

    public CmArkeaGroupSignatureStrategy(SignatureAlgorithm signatureAlgorithm) {
        super(signatureAlgorithm);
    }

    @Override
    public List<String> getHeadersToSign() {
        return PSU_HEADERS_TO_SIGN;
    }
}
