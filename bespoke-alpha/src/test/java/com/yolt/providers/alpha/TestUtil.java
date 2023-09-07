package com.yolt.providers.alpha;

import com.yolt.providers.alpha.common.auth.dto.AlphaAuthMeans;
import com.yolt.providers.common.util.KeyUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

public class TestUtil {

    public static PrivateKey createKey(final String keyString) {
        try {
            return KeyUtil.createPrivateKeyFromPemFormat(keyString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error creating private key");
        }
    }

    public static String readFile(final String filename) {
        try {
            URI fileURI = AlphaAuthMeans.class
                    .getClassLoader()
                    .getResource(filename)
                    .toURI();

            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Error reading private key");
        }
    }
}
