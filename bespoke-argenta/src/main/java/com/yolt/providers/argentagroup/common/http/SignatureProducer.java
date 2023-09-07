package com.yolt.providers.argentagroup.common.http;

import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SignatureProducer {

    private static Map<String, String> getSigningHeadersWithValue(HttpHeaders headers, List<String> headersToSign) {
        return headersToSign.stream()
                .filter(headers::containsKey)
                .map(header -> Map.entry(header, Objects.requireNonNull(headers.getFirst(header))))
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new)
                );
    }

    private static String createSigningString(final Map<String, String> signatureHeaders) {
        return signatureHeaders.keySet()
                .stream()
                .map(header -> header + ": " + signatureHeaders.get(header))
                .collect(Collectors.joining("\n"));
    }

    public String calculateSignature(final SignatureData signatureData) {
        Map<String, String> headersForSignature = getSigningHeadersWithValue(signatureData.getHeaders(), signatureData.getHeadersToSign());

        String signingString = createSigningString(headersForSignature);

        return String.format(
                "keyId=\"SN=%s,CA=%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                signatureData.getSigningCertificate().getSerialNumber(),
                signatureData.getSigningCertificate().getIssuerDN().getName().replace(" ", ""),
                signatureData.getSignatureAlgorithm().getHttpSignatureAlgorithm(),
                String.join(" ", headersForSignature.keySet()),
                signatureData.getSigner().sign(signingString.getBytes(), signatureData.getSigningKeyId(), signatureData.getSignatureAlgorithm()));
    }
}
