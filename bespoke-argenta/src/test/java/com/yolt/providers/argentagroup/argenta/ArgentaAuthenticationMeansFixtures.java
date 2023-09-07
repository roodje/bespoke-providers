package com.yolt.providers.argentagroup.argenta;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class ArgentaAuthenticationMeansFixtures {

    public static String TEST_TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    public static String TEST_SIGNING_KEY_ID = UUID.randomUUID().toString();


    public static Map<String, BasicAuthenticationMean> getSampleAuthenticationMeans() throws IOException, URISyntaxException {
        return Map.ofEntries(
                Map.entry(
                        DefaultAuthenticationMeans.TRANSPORT_CERTIFICATE_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), loadPemFile())
                ),
                Map.entry(
                        DefaultAuthenticationMeans.TRANSPORT_KEY_ID_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), TEST_TRANSPORT_KEY_ID)
                ),
                Map.entry(
                        DefaultAuthenticationMeans.SIGNING_CERTIFICATE_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), loadPemFile())
                ),
                Map.entry(
                        DefaultAuthenticationMeans.SIGNING_KEY_ID_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), TEST_SIGNING_KEY_ID)
                ),
                Map.entry(
                        DefaultAuthenticationMeans.API_KEY_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.API_KEY_STRING.getType(), "TEST_API_KEY")
                ),
                Map.entry(
                        DefaultAuthenticationMeans.CLIENT_ID_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "TEST_CLIENT_ID")
                )
        );
    }

    private static String loadPemFile() throws IOException, URISyntaxException {
        URI uri = ArgentaAuthenticationMeansFixtures.class
                .getClassLoader()
                .getResource("certificates/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
