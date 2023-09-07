package com.yolt.providers.stet.societegeneralegroup.common.http.signer;

import com.yolt.providers.common.cryptography.CavageHttpSigning;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.signer.signature.Fingerprint;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * this class was introduced because {@link CavageHttpSigning} adds whitespaces between signature header parameters
 * SG requires no whitespaces between signature parts and have different keyId generation
 */
@RequiredArgsConstructor
public class SocieteGeneraleHttpSigner {

    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private static final String PEM_FORMAT_EXTENSION = ".pem";

    private final Signer signer;
    private final UUID privateKid;
    private final SignatureAlgorithm signatureAlgorithm;
    private final DefaultProperties properties;

    public String signHeaders(final Map<String, String> headers,
                              final X509Certificate x509Certificate) {
        final List<String> headersKeys = new LinkedList<>();
        final List<String> headersData = new LinkedList<>();

        headers.forEach((key, value) -> {
            headersKeys.add(key.toLowerCase());
            headersData.add(key.toLowerCase() + ": " + value);
        });

        final String payload = String.join("\n", headersData);
        final String signature = signer.sign(payload.getBytes(), privateKid, signatureAlgorithm);
        String keyId;
        try {
            keyId = properties.getS3baseUrl() + "/" + new Fingerprint(x509Certificate.getEncoded()) + PEM_FORMAT_EXTENSION;
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException("Failed to create fingerprint from certificate in Societe Generale group");
        }
        return String.format(SIGNATURE_HEADER_FORMAT, keyId, signatureAlgorithm.getHttpSignatureAlgorithm(), String.join(" ", headersKeys), signature);
    }

}