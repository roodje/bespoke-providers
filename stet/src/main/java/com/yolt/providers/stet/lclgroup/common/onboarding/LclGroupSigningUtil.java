package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.yolt.providers.common.cryptography.CavageHttpSigning;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LclGroupSigningUtil {

    private static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .toFormatter();

    //TODO: Remove hardcoded OBJECT_MAPPER during migration to DefaultHttpClient (C4PO-6616)
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String DIGEST_HEADER = "Digest";
    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String SIGNATURE_HEADER = "Signature";
    private static final String DATE_HEADER = "Date";
    private static List<String> headersToSign = Arrays.asList(DATE_HEADER);

    // TODO temporary hack to proceed with production tests - this should be refactored ASAP - C4PO-6448
    // we should use the same object mapper that is used by RestTemplate to serialize body
    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(OffsetDateTime.class, new LclOffsetDateTimeSerializer(OFFSET_DATE_TIME_FORMATTER));
        module.addDeserializer(OffsetDateTime.class, new LclOffsetDateTimeDeserializer(OFFSET_DATE_TIME_FORMATTER));
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.registerModule(module);
    }

    public static HttpHeaders getRegistrationHeaders(String path,
                                                     HttpMethod method,
                                                     HttpHeaders headers,
                                                     Object requestBody,
                                                     SignatureData signatureData) {
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));
        String digest = requestBody != null ? getDigest(requestBody) : getDigest(new byte[0]);
        headers.set(DIGEST_HEADER, digest);

        final String lastExternalTraceId = ExternalTracingUtil.createLastExternalTraceId();
        headers.set(X_REQUEST_ID_HEADER, lastExternalTraceId);

        headers.set(SIGNATURE_HEADER, getSignatureHeaderValue(headersToSign, path, method, headers, signatureData));
        return headers;
    }

    @SneakyThrows
    private static String getDigest(Object requestBody) {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(getSerializedBody(requestBody));
        return "SHA-256=" + Base64.toBase64String(digest);
    }

    private static byte[] getSerializedBody(Object requestBody) throws JsonProcessingException {
        if (requestBody instanceof byte[]) {
            return (byte[]) requestBody;
        }
        return OBJECT_MAPPER.writeValueAsString(requestBody).getBytes(UTF_8);
    }

    private static String getSignatureHeaderValue(List<String> headersToSign, String path, HttpMethod method, HttpHeaders headers, SignatureData signatureData) {
        CavageHttpSigning cavageHttpSigning = new CavageHttpSigning(
                signatureData.getSigner(),
                signatureData.getSigningKeyId(),
                SignatureAlgorithm.SHA256_WITH_RSA);
        return cavageHttpSigning.signHeaders(filterHeadersToSign(headersToSign, headers), signatureData.getHeaderKeyId(), method.toString().toLowerCase(), path);
    }

    private static Map<String, String> filterHeadersToSign(List<String> headersToSign, HttpHeaders headers) {
        return headers
                .toSingleValueMap()
                .entrySet()
                .stream()
                .filter(header -> headersToSign.contains(header.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }
}
