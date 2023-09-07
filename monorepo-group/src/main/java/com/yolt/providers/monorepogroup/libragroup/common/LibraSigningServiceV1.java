package com.yolt.providers.monorepogroup.libragroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupConsentRequest;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class LibraSigningServiceV1 implements LibraSigningService {
    private static final String DIGEST_HEADER_NAME = "Digest";
    private static final String SIGNATURE = "Signature";
    private static final String X_REQUEST_ID = "X-Request-ID";

    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private static final String HEADERS_TO_SIGN = "digest x-request-id";
    private static final String SIGNING_STRING = """
            digest: %s
            x-request-id: %s""";
    private static final String HASHING_ALGORITHM = "SHA-256";
    private static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";

    private final ObjectMapper objectMapper;

    @Override
    public HttpHeaders getSigningHeaders(MultiValueMap<String, String> payload,
                                         String signingCertificateSerialNumber,
                                         UUID signingKeyId,
                                         String signingCertificate,
                                         Signer signer) throws TokenInvalidException {
        return getSigningHeaders(
                calculateDigest(payload),
                signingCertificateSerialNumber,
                signingKeyId,
                signingCertificate,
                signer);
    }

    @Override
    public HttpHeaders getSigningHeaders(LibraGroupConsentRequest payload,
                                         String signingCertificateSerialNumber,
                                         UUID signingKeyId,
                                         String signingCertificate,
                                         Signer signer) throws TokenInvalidException {
        return getSigningHeaders(
                calculateDigest(payload),
                signingCertificateSerialNumber,
                signingKeyId,
                signingCertificate,
                signer);
    }

    private String calculateDigest(final MultiValueMap<String, String> body) throws TokenInvalidException {
        return calculateDigest(serializeForm(body).getBytes(UTF_8));
    }

    private HttpHeaders getSigningHeaders(String digest,
                                          String signingCertificateSerialNumber,
                                          UUID signingKeyId,
                                          String signingCertificate,
                                          Signer signer) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        httpHeaders.add(DIGEST_HEADER_NAME, digest);
        httpHeaders.add(TPP_SIGNATURE_CERTIFICATE, signingCertificate);
        httpHeaders.add(SIGNATURE, getSignature(httpHeaders, signingCertificateSerialNumber, signingKeyId, signer));
        return httpHeaders;
    }

    private String calculateDigest(LibraGroupConsentRequest request) throws TokenInvalidException {
        try {
            return calculateDigest(objectMapper.writeValueAsString(request).getBytes(UTF_8));
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Problem with digest calculation", e);
        }
    }

    private String calculateDigest(byte[] body) throws TokenInvalidException {
        try {
            byte[] messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM)
                    .digest(body);
            return HASHING_ALGORITHM + "=" + Base64.toBase64String(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenInvalidException("Problem with digest calculation", e);
        }
    }

    private String getSignature(HttpHeaders headers,
                                String signingCertificateSerialNumber,
                                UUID signingKeyId,
                                Signer signer) {
        String algorithm = "rsa-sha256";
        String signingString = fillSigningString(headers);
        String signedHeaders = signer.sign(signingString.getBytes(UTF_8), signingKeyId, SignatureAlgorithm.SHA256_WITH_RSA);

        return String.format(SIGNATURE_HEADER_FORMAT, signingCertificateSerialNumber, algorithm, HEADERS_TO_SIGN, signedHeaders);
    }

    private String fillSigningString(HttpHeaders headers) {
        return String.format(SIGNING_STRING,
                headers.getFirst(DIGEST_HEADER_NAME),
                headers.getFirst(X_REQUEST_ID));
    }

    private static String serializeForm(MultiValueMap<String, String> formData) {
        StringBuilder builder = new StringBuilder();
        formData.forEach((name, values) ->
                values.forEach(value -> {

                    if (builder.length() != 0) {
                        builder.append('&');
                    }
                    builder.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
                    if (value != null) {
                        builder.append('=');
                        builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                    }
                }));

        return builder.toString();
    }
}
