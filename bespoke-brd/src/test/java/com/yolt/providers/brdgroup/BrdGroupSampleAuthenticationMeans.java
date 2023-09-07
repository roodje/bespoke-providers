package com.yolt.providers.brdgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.brdgroup.common.BrdGroupAuthenticationMeans.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.brdgroup.common.BrdGroupAuthenticationMeans.TRANSPORT_KEY_ID_NAME;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;

public class BrdGroupSampleAuthenticationMeans {

    public static Map<String, BasicAuthenticationMean> get() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), UUID.randomUUID().toString()));
        return authenticationMeans;
    }

    private static String loadPemFile(final String filename) {
        try {
            URI uri = BrdGroupSampleAuthenticationMeans.class
                    .getClassLoader()
                    .getResource("certificates/" + filename)
                    .toURI();
            Path filePath = new File(uri).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred loading the fake certificate");
        }
    }
}
