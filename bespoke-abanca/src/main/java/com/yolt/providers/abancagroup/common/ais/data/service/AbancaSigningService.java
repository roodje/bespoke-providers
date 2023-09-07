package com.yolt.providers.abancagroup.common.ais.data.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

public class AbancaSigningService {

    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private static final String HEADERS_TO_SIGN = "request-target date digest x-request-id";
    private static final String SIGNING_STRING = """
            request-target: %s
            date: %s
            digest: %s
            x-request-id: %s
            """;

    public String getSignature(final HttpHeaders headers, final String signatureKeyId,
                               final UUID signingKeyId, final Signer signer) {
        String algorithm = "rsa-sha256";
        String signingString = fillSigningString(headers);
        String signedHeaders = signer.sign(signingString.getBytes(), signingKeyId, SignatureAlgorithm.SHA256_WITH_RSA);

        return String.format(SIGNATURE_HEADER_FORMAT, signatureKeyId, algorithm, HEADERS_TO_SIGN, signedHeaders);
    }

    private String fillSigningString(HttpHeaders headers) {
        return String.format(SIGNING_STRING,
                retrieveHeader(headers, "Request-Target"),
                retrieveHeader(headers, "Date"),
                retrieveHeader(headers, "Digest"),
                retrieveHeader(headers, "X-Request-ID"));
    }

    private String retrieveHeader(HttpHeaders headers, String value) {
        return headers.get(value).get(0);
    }

}