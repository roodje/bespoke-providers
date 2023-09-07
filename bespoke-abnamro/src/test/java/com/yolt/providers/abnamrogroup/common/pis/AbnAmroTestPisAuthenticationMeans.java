package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.abnamrogroup.AbnAmroDataProviderIntegrationTest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

public class AbnAmroTestPisAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_TRANSPORT_KEY_ID = "client-transport-private-key-id";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";
    public static final String API_KEY_NAME = "api-key";

    private Map<String, BasicAuthenticationMean> authMeans;

    public AbnAmroTestPisAuthenticationMeans() {
        authMeans = new HashMap<>();
        authMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "TPP_test"));
        authMeans.put(API_KEY_NAME, new BasicAuthenticationMean(API_KEY.getType(), "7zacIF8Cu5o3XF4gUll4sRGuI2gDYiCA"));
        authMeans.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), UUID.randomUUID().toString()));
        authMeans.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificates()));
    }

    public Map<String, BasicAuthenticationMean> getAuthMeans() {
        return authMeans;
    }

    private String readCertificates() {
        try {
            URI fileURI = AbnAmroDataProviderIntegrationTest.class
                    .getClassLoader()
                    .getResource("certificates/yolt_certificate.pem")
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
