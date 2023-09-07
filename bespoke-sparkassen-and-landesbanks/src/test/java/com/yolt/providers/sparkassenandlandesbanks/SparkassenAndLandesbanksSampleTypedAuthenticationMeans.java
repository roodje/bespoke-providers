package com.yolt.providers.sparkassenandlandesbanks;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.experimental.UtilityClass;

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

@UtilityClass
public class SparkassenAndLandesbanksSampleTypedAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";

    public static Map<String, BasicAuthenticationMean> createTestAuthenticationMeans() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "SOME_CLIENT_ID"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), "59688eb9-930f-469a-ad5c-43b0e92d7b89"));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile()));
        return authenticationMeans;
    }

    private static String loadPemFile() {
        try {
            URI uri = SparkassenAndLandesbanksSampleTypedAuthenticationMeans.class
                    .getClassLoader()
                    .getResource("certificates/fake-certificate.pem")
                    .toURI();
            Path filePath = new File(uri).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Could not read file");
        }
    }
}
