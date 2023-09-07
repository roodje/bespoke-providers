package com.yolt.providers.argentagroup.common.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.argentagroup.common.exception.MalformedObjectException;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
public class JsonBodyDigestProducer {

    private final ObjectMapper objectMapper;

    public String calculateSHA256Digest(final Object payload) {
        try {
            byte[] serializedRequestBody = getSerializedRequestBody(payload);
            return "SHA-256=" + Base64.toBase64String(MessageDigest.getInstance("SHA-256").digest(serializedRequestBody));
        } catch (JsonProcessingException e) {
            throw new MalformedObjectException("Couldn't serialize provided object");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    private byte[] getSerializedRequestBody(Object payload) throws JsonProcessingException {
        if (payload instanceof byte[]) {
            return (byte[]) payload;
        }
        return objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
    }
}
