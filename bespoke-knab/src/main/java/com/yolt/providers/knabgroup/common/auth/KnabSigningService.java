package com.yolt.providers.knabgroup.common.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.knabgroup.common.exception.UnexpectedJsonElementException;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class KnabSigningService {

    private final ObjectMapper objectMapper;

    public String calculateDigest(Object requestBody) {
        try {
            byte[] serializedRequestBody = getSerializedRequestBody(requestBody);
            return "SHA-256=" + Base64.toBase64String(MessageDigest.getInstance("SHA-256").digest(serializedRequestBody));
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Couldn't convert requestBody object to string");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    public String calculateSignature(HttpHeaders headers, SignatureData signatureData, List<String> headersInSignature) {
        Validate.notNull(signatureData);

        Map<String, String> signingHeaders = getSigningHeadersWithValue(headers);

        String allHeaders = String.join(" ", headersInSignature).toLowerCase();
        String signingString = createSigningString(signingHeaders, headersInSignature);
        SignatureAlgorithm signingAlgorythm = SignatureAlgorithm.SHA256_WITH_RSA;
        String signedHeaders = signatureData.getSigner().sign(signingString.getBytes(), signatureData.getSigningKeyId(), signingAlgorythm);

        Validate.notEmpty(signedHeaders);

        return String.format("keyId=\"SN=%s,CA=%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                signatureData.getSigningCertificate().getSerialNumber().toString(),
                signatureData.getSigningCertificate().getSubjectDN().getName().replace(" ", ""),
                signingAlgorythm.getHttpSignatureAlgorithm(),
                allHeaders,
                signedHeaders);
    }

    private Map<String, String> getSigningHeadersWithValue(HttpHeaders headers) {
        return headers.toSingleValueMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    private byte[] getSerializedRequestBody(Object requestBody) throws JsonProcessingException {
        if (requestBody instanceof byte[]) {
            return (byte[]) requestBody;
        }
        return objectMapper.writeValueAsString(requestBody).getBytes(StandardCharsets.UTF_8);
    }

    private String createSigningString(final Map<String, String> signatureHeaders, List<String> headersInSignature) {
        return headersInSignature
                .stream()
                .map(String::toLowerCase)
                .map(header -> {
                    Validate.notNull(signatureHeaders.get(header));
                    return header + ": " + signatureHeaders.get(header);
                })
                .collect(Collectors.joining("\n"));
    }
}
