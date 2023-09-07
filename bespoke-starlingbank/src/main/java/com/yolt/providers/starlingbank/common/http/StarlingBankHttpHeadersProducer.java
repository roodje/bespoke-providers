package com.yolt.providers.starlingbank.common.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.starlingbank.common.http.signer.StarlingBankHttpSigner;
import org.springframework.http.HttpHeaders;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Base64;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class StarlingBankHttpHeadersProducer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DATE_HEADER = "Date";
    private static final String DIGEST_HEADER = "Digest";

    private final StarlingBankHttpSigner signer;
    private final Clock clock;

    public StarlingBankHttpHeadersProducer(StarlingBankHttpSigner signer, Clock clock) {
        this.signer = signer;
        this.clock = clock;
    }

    public HttpHeaders createSigningHeaders(String accessToken, Object body, String signingKeyHeaderId, String relativeUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(DATE_HEADER, getCurrentDateTime());
        headers.add(DIGEST_HEADER, getDigest(body));
        headers.add(AUTHORIZATION, String.format("Bearer %s;Signature %s", accessToken, getSignature(headers, signingKeyHeaderId, relativeUrl)));
        return headers;
    }

    HttpHeaders createEncodedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        return headers;
    }

    HttpHeaders createAuthorizationHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private String getDigest(Object body) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-512").digest(getSerializedBody(body));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm not configured properly for digest calculation", e);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Object mapper failed during serialization of request body");
        }
    }

    private byte[] getSerializedBody(Object requestBody) throws JsonProcessingException {
        if (requestBody instanceof byte[]) {
            return (byte[]) requestBody;
        }
        return OBJECT_MAPPER.writeValueAsString(requestBody).getBytes(UTF_8);
    }

    private String getCurrentDateTime() {
        return new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
                .appendOffset("+HH:MM", "Z")
                .toFormatter()
                .format(ZonedDateTime.ofInstant(Instant.now(clock), ZoneId.of("Europe/London")));
    }

    private String getSignature(HttpHeaders headers, String signingKeyHeaderId, String relativeUrl) {
        return signer.createSignature(headers.toSingleValueMap(), signingKeyHeaderId, PUT.toString(), relativeUrl);
    }
}
