package com.yolt.providers.amexgroup.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.StringJoiner;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmexMacHeaderUtils {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String NONCE_SUFFIX = ":AMEX";
    private static final String MAC_TOKEN = "MAC id=\"%s\",ts=\"%s\",nonce=\"%s\",mac=\"%s\"";
    private static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

    public static String generateAuthMacToken(String clientId,
                                              String clientSecret,
                                              String grantType) throws NoSuchAlgorithmException, InvalidKeyException {

        String nonce = UUID.randomUUID().toString() + NONCE_SUFFIX;
        String timeInMillis = String.valueOf((System.currentTimeMillis()));

        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(clientId);
        stringJoiner.add(timeInMillis);
        stringJoiner.add(nonce);
        stringJoiner.add(grantType + "\n");

        String signatureStr = encode(clientSecret, stringJoiner);
        return String.format(MAC_TOKEN, clientId, timeInMillis, nonce, signatureStr);
    }

    public static String generateDataMacToken(String accessToken,
                                              String macKey,
                                              String httpMethod,
                                              String host,
                                              String port,
                                              String resourcePath)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {

        String nonce = UUID.randomUUID().toString() + NONCE_SUFFIX;
        String timeInMillis = String.valueOf((System.currentTimeMillis()));

        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(timeInMillis);
        stringJoiner.add(nonce);
        stringJoiner.add(httpMethod);
        stringJoiner.add(URLEncoder.encode(resourcePath, CHARSET_UTF_8.name()));
        stringJoiner.add(host);
        stringJoiner.add(port + "\n\n");

        String signatureStr = encode(macKey, stringJoiner);
        return String.format(MAC_TOKEN, accessToken, timeInMillis, nonce, signatureStr);
    }

    private static String encode(String seed, StringJoiner stringJoiner)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(seed.getBytes(CHARSET_UTF_8), HMAC_SHA256_ALGORITHM));
        return Base64.getEncoder().encodeToString(mac.doFinal(stringJoiner.toString().getBytes(CHARSET_UTF_8)));
    }
}