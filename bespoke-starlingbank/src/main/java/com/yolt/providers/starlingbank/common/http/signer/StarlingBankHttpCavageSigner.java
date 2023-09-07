package com.yolt.providers.starlingbank.common.http.signer;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class StarlingBankHttpCavageSigner implements StarlingBankHttpSigner {

    private final Signer signer;
    private final UUID privateKid;
    private final SignatureAlgorithm signatureAlgorithm;

    /**
     * This class is a copy of CavageHttpSigning in provider commons.
     * It fixes some minor changes between Starling documentation and common practise.
     * Candidate to be removed, if possible.
     */
    public StarlingBankHttpCavageSigner(Signer signer, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
        if (signer == null) {
            throw new IllegalStateException("Signer is marked non-null but is null");
        }
        if (privateKid == null) {
            throw new IllegalStateException("PrivateKid is marked non-null but is null");
        }
        if (signatureAlgorithm == null) {
            throw new IllegalStateException("signatureAlgorithm is marked non-null but is null");
        }
        this.signer = signer;
        this.privateKid = privateKid;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public String createSignature(Map<String, String> headers, String headerKeyId, String httpMethod, String httpEndpoint) {
        if (headers == null) {
            throw new IllegalStateException("headers is marked non-null but is null");
        }
        if (headerKeyId == null) {
            throw new IllegalStateException("headerKeyId is marked non-null but is null");
        }
        if (httpMethod == null) {
            throw new IllegalStateException("httpMethod is marked non-null but is null");
        }
        if (httpEndpoint == null) {
            throw new IllegalStateException("httpEndpoint is marked non-null but is null");
        }
        List<String> headersKeys = new LinkedList<>();
        List<String> headersData = new LinkedList<>();
        headersKeys.add("(request-target)");
        headersData.add(String.format("(request-target): %s %s", httpMethod.toLowerCase(), httpEndpoint));

        headers.forEach((key, value) -> {
            headersKeys.add(key);
            headersData.add(key + ": " + value);
        });
        String payload = String.join("\n", headersData);
        String signature = signer.sign(payload.getBytes(), privateKid, signatureAlgorithm);
        return String.format("keyid=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", headerKeyId, signatureAlgorithm.getHttpSignatureAlgorithm(), String.join(" ", headersKeys), signature);
    }
}