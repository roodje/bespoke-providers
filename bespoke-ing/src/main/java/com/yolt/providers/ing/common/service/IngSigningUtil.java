package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IngSigningUtil {

    private static final String HASHING_ALGORITHM = "SHA-256";
    private static final String SIGNING_HEADERS_FORMAT = "(request-target): %s %s\n%s";
    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"(request-target) %s\",signature=\"%s\"";
    private static final List<String> HEADERS_TO_SIGN = Arrays.asList("Date", "Digest", "X-Request-ID");

    public String getSignature(final HttpMethod method, final String url, final HttpHeaders headers, final String signatureKeyId,
                               final UUID signingKeyId, final Signer signer) {
        String algorithm = "rsa-sha256";
        Map<String, String> signingHeaders = getSigningHeadersWithValues(headers);
        String headersWithDelimiter = String.join(" ", signingHeaders.keySet());
        String signingString = createSigningHeadersString(method, url, signingHeaders);
        String signedHeaders = signer.sign(signingString.getBytes(), signingKeyId, SignatureAlgorithm.SHA256_WITH_RSA);

        return String.format(SIGNATURE_HEADER_FORMAT, signatureKeyId, algorithm, headersWithDelimiter, signedHeaders);
    }

    public String getDigest(final MultiValueMap<String, Object> body) {
        byte[] byteBody = serializeForm(body, UTF_8).getBytes(UTF_8);
        try {
            byte[] messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM)
                    .digest(byteBody);
            return HASHING_ALGORITHM + "=" + Base64.toBase64String(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
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

    private static Map<String, String> getSigningHeadersWithValues(final HttpHeaders headers) {
        //TreeMap is sorted alphabetically by default. It is ok for us, because for authorization we need headers
        // in order 'date digest x-ing-reqid' and for fetching data in order 'date digest x-request-id'
        return headers.toSingleValueMap()
                .entrySet()
                .stream()
                .filter(header -> HEADERS_TO_SIGN.contains(header.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    private static String createSigningHeadersString(final HttpMethod method, final String url, final Map<String, String> signatureHeaders) {
        String signatureHeadersString = signatureHeaders.entrySet()
                .stream()
                .map(header -> header.getKey() + ": " + header.getValue())
                .collect(Collectors.joining("\n"));

        return String.format(SIGNING_HEADERS_FORMAT, method.toString().toLowerCase(), url, signatureHeadersString);
    }

    private static String serializeForm(MultiValueMap<String, Object> formData, Charset charset) {
        StringBuilder builder = new StringBuilder();
        formData.forEach((name, values) ->
                values.forEach(value -> {
                    try {
                        if (builder.length() != 0) {
                            builder.append('&');
                        }
                        builder.append(URLEncoder.encode(name, charset.name()));
                        if (value != null) {
                            builder.append('=');
                            builder.append(URLEncoder.encode(String.valueOf(value), charset.name()));
                        }
                    } catch (UnsupportedEncodingException ex) {
                        throw new IllegalStateException(ex);
                    }
                }));

        return builder.toString();
    }
}