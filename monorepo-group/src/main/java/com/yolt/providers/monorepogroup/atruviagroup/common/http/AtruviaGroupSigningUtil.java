package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class AtruviaGroupSigningUtil {

    private static final String HASHING_ALGORITHM = "SHA-256";
    private static final String SIGNING_HEADERS_FORMAT = "%s";
    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private static final String KEY_ID_FORMAT = "SN=%s,CA=%s";
    private static final List<String> HEADERS_TO_SIGN = Arrays.asList("Digest", "X-Request-ID", "PSU-ID");


    public String getSignature(HttpHeaders headers,
                               X509Certificate x509Certificate,
                               UUID signingKeyId,
                               Signer signer,
                               List<String> headersToUseInSignature) {
        String algorithm = "rsa-sha256";
        Map<String, String> signingHeaders = getSigningHeadersWithValues(headers, headersToUseInSignature);
        String headersWithDelimiter = String.join(" ", signingHeaders.keySet());
        String signingString = createSigningHeadersString(signingHeaders);
        String signedHeaders = signer.sign(signingString.getBytes(), signingKeyId, SignatureAlgorithm.SHA256_WITH_RSA);
        var cn = x509Certificate.getIssuerDN().getName().replace(" ", "");
        var serialNo = x509Certificate.getSerialNumber();
        var keyId = String.format(KEY_ID_FORMAT, serialNo, cn);
        return String.format(SIGNATURE_HEADER_FORMAT, keyId, algorithm, headersWithDelimiter, signedHeaders);
    }

    public String getSignature(HttpHeaders headers,
                               X509Certificate x509Certificate,
                               UUID signingKeyId,
                               Signer signer) {
        return getSignature(headers, x509Certificate, signingKeyId, signer, HEADERS_TO_SIGN);
    }

    public String getDigest(final byte[] body) {
        try {
            byte[] messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM)
                    .digest(body);
            return HASHING_ALGORITHM + "=" + Base64.toBase64String(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private static Map<String, String> getSigningHeadersWithValues(HttpHeaders headers,
                                                                   List<String> headersToSign) {
        //TreeMap is sorted alphabetically by default. It is ok for us, because for authorization we need headers
        //  in order 'date digest x-request-id'
        return headers.toSingleValueMap()
                .entrySet()
                .stream()
                .filter(header -> headersToSign.contains(header.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    private static String createSigningHeadersString(Map<String, String> signatureHeaders) {
        String signatureHeadersString = signatureHeaders.entrySet()
                .stream()
                .map(header -> header.getKey() + ": " + header.getValue())
                .collect(Collectors.joining("\n"));
        return String.format(SIGNING_HEADERS_FORMAT, signatureHeadersString);
    }
}