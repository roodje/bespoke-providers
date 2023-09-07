package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpHeaderDigest {

    public static String createDigestHeaderValue(byte[] body) {
        if (body == null) {
            body = new byte[0];
        }
        return "SHA-256=" + base64(sha256(body));
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    private static byte[] sha256(byte[] body) {
        return MessageDigest.getInstance("SHA-256").digest(body);
    }

    private static String base64(byte[] digest) {
        return new String(Base64.getEncoder().encode(digest), StandardCharsets.UTF_8);
    }
}
