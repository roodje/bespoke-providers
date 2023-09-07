package com.yolt.providers.cbiglobe.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.exception.CbiGlobeMalformedObjectException;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.yolt.providers.cbiglobe.common.util.CbiGlobeHttpHeaderUtil.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CbiGlobeSigningUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> SIGNING_HEADERS = Arrays.asList(DIGEST_HEADER, X_REQUEST_ID_HEADER, DATE_HEADER, TPP_REDIRECT_URI_HEADER, PSU_ID);

    public static String getDigest(Object requestBody) {
        try {
            byte[] serializedRequestBody = getSerializedRequestBody(requestBody);
            return "sha-512=" + Base64.toBase64String(MessageDigest.getInstance("SHA-512").digest(serializedRequestBody));
        } catch (JsonProcessingException e) {
            throw new CbiGlobeMalformedObjectException("Couldn't convert requestBody object to string");
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

    public static String getSignature(HttpHeaders headers, SignatureData signatureData) {
        Map<String, String> signingHeaders = getSigningHeadersWithValue(headers);
        String allHeaders = String.join(" ", signingHeaders.keySet());
        String signingString = createSigningString(signingHeaders);
        String signedHeaders = signatureData.getSigner().sign(signingString.getBytes(), signatureData.getSigningKeyId(), SignatureAlgorithm.SHA512_WITH_RSA);

        return String.format("keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                signatureData.getSigningCertificate().getSerialNumber().toString(),
                SignatureAlgorithm.SHA512_WITH_RSA.getHttpSignatureAlgorithm(),
                allHeaders,
                signedHeaders);
    }

    private static Map<String, String> getSigningHeadersWithValue(HttpHeaders headers) {
        return headers.toSingleValueMap()
                .entrySet()
                .stream()
                .filter(header -> SIGNING_HEADERS.contains(header.getKey()))
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
}
