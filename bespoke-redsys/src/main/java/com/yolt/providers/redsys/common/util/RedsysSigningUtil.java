package com.yolt.providers.redsys.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.redsys.common.exception.RedsysMalformedObjectException;
import com.yolt.providers.redsys.common.model.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.experimental.UtilityClass;
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

@UtilityClass
public class RedsysSigningUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String calculateDigest(Object requestBody) {
        try {
            byte[] serializedRequestBody = getSerializedRequestBody(requestBody);
            return "SHA-256=" + Base64.toBase64String(MessageDigest.getInstance("SHA-256").digest(serializedRequestBody));
        } catch (JsonProcessingException e) {
            throw new RedsysMalformedObjectException("Couldn't convert requestBody object to string");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    private static byte[] getSerializedRequestBody(Object requestBody) throws JsonProcessingException {
        if (requestBody instanceof byte[]) {
            return (byte[]) requestBody;
        }
        return OBJECT_MAPPER.writeValueAsString(requestBody).getBytes(StandardCharsets.UTF_8);
    }

    public static String calculateSignature(HttpHeaders headers, SignatureData signatureData, List<String> headersInSignature) {
        Validate.notNull(signatureData);

        Map<String, String> signingHeaders = getSigningHeadersWithValue(headers);

        String allHeaders = String.join(" ", headersInSignature).toLowerCase();
        String signingString = createSigningString(signingHeaders, headersInSignature);
        String signedHeaders = signatureData.getSigner().sign(signingString.getBytes(), signatureData.getSigningKeyId(), SignatureAlgorithm.SHA256_WITH_RSA);

        Validate.notEmpty(signedHeaders);

        return String.format("keyId=\"SN=%s,CA=%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                signatureData.getSigningCertificate().getSerialNumber().toString(),
                signatureData.getSigningCertificate().getSubjectDN().getName().replace(" ", ""),
                "SHA-256", //TODO: Warn. It can be also SHA-RSA-256
                allHeaders,
                signedHeaders);
    }

    private static Map<String, String> getSigningHeadersWithValue(HttpHeaders headers) {
        return headers.toSingleValueMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    private static String createSigningString(final Map<String, String> signatureHeaders, List<String> headersInSignature) {
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
