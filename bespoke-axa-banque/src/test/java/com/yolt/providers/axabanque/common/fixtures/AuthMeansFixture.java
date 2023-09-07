package com.yolt.providers.axabanque.common.fixtures;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans.TRANSPORT_CERTIFICATE;
import static com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans.TRANSPORT_KEY_ID;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;

public class AuthMeansFixture {
    public static GroupAuthenticationMeans getAuthMeans(String clientId) {
        return new GroupAuthenticationMeans(null, null, clientId);
    }

    public static Map<String, BasicAuthenticationMean> getAuthMeansMap(String transportKeyIdValue) {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), transportKeyIdValue));
        means.put(TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(),
                readCertificate("certificates/yolt_certificate_transport.pem")));
        return means;
    }

    private static String readCertificate(String certificatePath) {
        try {
            URI fileURI = AuthMeansFixture.class
                    .getClassLoader()
                    .getResource(certificatePath)
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
