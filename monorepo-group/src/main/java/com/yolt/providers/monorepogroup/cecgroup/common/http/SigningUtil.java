package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * As the bank's doc doesn't specify any details on signature calculation,
 * this implementation is copied from one of the existing banks in the BG standard
 * https://git.yolt.io/providers/bespoke-rabobank/-/blob/master/src/main/java/com/yolt/providers/rabobank/SigningUtil.java
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SigningUtil {

    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";

    public static String getSigningString(final Signer signer, final HttpHeaders headers, final String certificateSerialNumber, final UUID signingKid, final List<String> headersToSign) {
        Map<String, String> signingHeaders = getSigningHeadersWithValue(headers, headersToSign);
        String allHeaders = String.join(" ", signingHeaders.keySet());
        String signingString = createSigningString(signingHeaders);
        String signedHeaders = signer.sign(signingString.getBytes(), signingKid, SignatureAlgorithm.SHA512_WITH_RSA);

        return String.format(SIGNATURE_HEADER_FORMAT, certificateSerialNumber, "rsa-sha512", allHeaders, signedHeaders);
    }

    private static Map<String, String> getSigningHeadersWithValue(final HttpHeaders headers, List<String> headersToSign) {
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

    private static String createSigningString(final Map<String, String> signatureHeaders) {
        return signatureHeaders.entrySet()
                .stream()
                .map(header -> header.getKey() + ": " + header.getValue())
                .collect(Collectors.joining("\n"));
    }

    public static String getDigest(final byte[] body) {
        try {
            return "sha-512=" + Base64.toBase64String(MessageDigest.getInstance("SHA-512").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }
}
