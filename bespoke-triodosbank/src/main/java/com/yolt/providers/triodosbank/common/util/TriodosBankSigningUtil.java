package com.yolt.providers.triodosbank.common.util;

import com.yolt.providers.triodosbank.common.model.domain.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import javax.security.auth.x500.X500Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TriodosBankSigningUtil {

    public static String getDigest(byte[] body) {
        try {
            if (body == null) {
                body = new byte[0];
            }

            return "SHA-256=" + Base64.toBase64String(MessageDigest.getInstance("SHA-256").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    public static String getSignature(HttpHeaders headers, SignatureData signatureData) {
        List<String> headersToSign = Arrays.asList("digest", "x-request-id");
        Map<String, String> signingHeaders = getSigningHeadersWithValue(headers, headersToSign);
        String allHeaders = String.join(" ", signingHeaders.keySet());
        String signingString = createSigningString(signingHeaders);
        String signedHeaders = signatureData.getSigner().sign(signingString.getBytes(), signatureData.getSigningKeyId(), SignatureAlgorithm.SHA256_WITH_RSA);

        return String.format("keyId=\"SN=%s,CA=%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                signatureData.getSigningCertificate().getSerialNumber().toString(16),
                signatureData.getSigningCertificate().getIssuerX500Principal().getName(X500Principal.RFC1779),
                SignatureAlgorithm.SHA256_WITH_RSA.getHttpSignatureAlgorithm(),
                allHeaders,
                signedHeaders);
    }

    private static Map<String, String> getSigningHeadersWithValue(final HttpHeaders headers, List<String> headersToSign) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Map<String, String> headersMap = headers.toSingleValueMap();

        for (String header : headersToSign) {
            if (headersMap.get(header) != null) {
                map.put(header.toLowerCase(), headersMap.get(header));
            }
        }

        return map;
    }

    private static String createSigningString(final Map<String, String> signatureHeaders) {
        return signatureHeaders.entrySet()
                .stream()
                .map(header -> header.getKey() + ": " + header.getValue())
                .collect(Collectors.joining("\n"));
    }
}
