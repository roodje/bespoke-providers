package com.yolt.providers.knabgroup.samples;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

public class SampleAuthenticationMeans {

    // keys name assumptions for purpose of tests
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String SIGNING_PRIVATE_KEY_ID = "signing-private-key-id";
    private static final String TRANSPORT_PRIVATE_KEY_ID = "transport-private-key-id";
    private static final String SIGNING_CERTIFICATE = "signing-certificate";
    private static final String TRANSPORT_CERTIFICATE = "transport-certificate";

    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    static {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "d3de0198-6738-4784-92d0-a3e5e0894413"));
        authenticationMeans.put(CLIENT_SECRET, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "d3de0198-6738-4784-92d0-a3e5e0894414"));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "d3de0198-6738-4784-92d0-a3e5e0894415"));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "d3de0198-6738-4784-92d0-a3e5e0894416"));
        try {
            authenticationMeans.put(SIGNING_CERTIFICATE, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), loadPemFile()));
            authenticationMeans.put(TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), loadPemFile()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, BasicAuthenticationMean> getSampleAuthenticationMeans() {
        return authenticationMeans;
    }

    private static String loadPemFile() throws IOException, URISyntaxException {
        URI uri = SampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
