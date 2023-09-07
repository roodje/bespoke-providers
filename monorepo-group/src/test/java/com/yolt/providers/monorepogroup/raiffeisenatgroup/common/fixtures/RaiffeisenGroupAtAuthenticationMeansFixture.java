package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.fixtures;


import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans.*;

public class RaiffeisenGroupAtAuthenticationMeansFixture {

    public static Map<String, BasicAuthenticationMean> getAuthMeansMap(String transportKeyIdValue, String clientId) {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(TRANSPORT_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), transportKeyIdValue));
        means.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(),
                readCertificate("certificates/fake-certificate.pem")));
        means.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), clientId));
        return means;
    }

    private static String readCertificate(String certificatePath) {
        try {
            URI fileURI = RaiffeisenGroupAtAuthenticationMeansFixture.class
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
