package com.yolt.providers.amexgroup.common.utils;

import org.junit.jupiter.api.Test;

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

import static org.assertj.core.api.Assertions.assertThat;


public class AmexMacHeaderUtilsV2Test {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;
    String clientId = "some-uuid";
    String clientSecret = "some-secret";
    String grantTypeAuthorizationCode = "authorization_code";
    String accessToken = "the-access-token";
    String macKey = "the-mac-key";
    String httpMethod = "GET";
    String host = "127.0.0.1";
    String port = "8080";
    String resourcePath = "/resources/somepath";
    AmexMacHeaderUtilsV2 amexMacHeaderUtils = new AmexMacHeaderUtilsV2();

    @Test
    public void shouldGenerateCorrectHeaderMacToken() throws InvalidKeyException, NoSuchAlgorithmException {
        // given
        String generatedMac = amexMacHeaderUtils.generateAuthMacToken(clientId, clientSecret, grantTypeAuthorizationCode);
        String id = generatedMac.substring(generatedMac.indexOf("id=") + 4, generatedMac.indexOf(",", generatedMac.indexOf("id=")) - 1);
        String ts = generatedMac.substring(generatedMac.indexOf("ts=") + 4, generatedMac.indexOf(",", generatedMac.indexOf("ts=")) - 1);
        String nonce = generatedMac.substring(generatedMac.indexOf("nonce=") + 7, generatedMac.indexOf(",", generatedMac.indexOf("nonce=")) - 1);
        String mac = generatedMac.substring(generatedMac.indexOf("mac=") + 5, generatedMac.length() - 1);

        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(id);
        stringJoiner.add(ts);
        stringJoiner.add(nonce);
        stringJoiner.add(grantTypeAuthorizationCode + "\n");

        // when
        String signatureStr = encode(clientSecret, stringJoiner);

        // then
        assertThat(signatureStr).isEqualTo(mac);
    }

    @Test
    public void shouldGenerateCorrectFetchDataHeaderMacToken() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        // given
        String generatedMac = amexMacHeaderUtils.generateDataMacToken(accessToken, macKey, httpMethod, host, port, resourcePath);
        String ts = generatedMac.substring(generatedMac.indexOf("ts=") + 4, generatedMac.indexOf(",", generatedMac.indexOf("ts=")) - 1);
        String nonce = generatedMac.substring(generatedMac.indexOf("nonce=") + 7, generatedMac.indexOf(",", generatedMac.indexOf("nonce=")) - 1);
        String mac = generatedMac.substring(generatedMac.indexOf("mac=") + 5, generatedMac.length() - 1);

        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(ts);
        stringJoiner.add(nonce);
        stringJoiner.add(httpMethod);
        stringJoiner.add(URLEncoder.encode(resourcePath, CHARSET_UTF_8.name()));
        stringJoiner.add(host);
        stringJoiner.add(port + "\n\n");

        // when
        String signatureStr = encode(macKey, stringJoiner);

        // then
        assertThat(signatureStr).isEqualTo(mac);
    }

    private String encode(String seed, StringJoiner stringJoiner)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(seed.getBytes(CHARSET_UTF_8), HMAC_SHA256_ALGORITHM));
        return Base64.getEncoder().encodeToString(mac.doFinal(stringJoiner.toString().getBytes(CHARSET_UTF_8)));
    }
}



