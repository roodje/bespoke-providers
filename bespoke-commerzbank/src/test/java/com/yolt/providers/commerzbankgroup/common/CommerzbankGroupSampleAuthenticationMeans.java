package com.yolt.providers.commerzbankgroup.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.commerzbankgroup.common.authmeans.CommerzbankGroupAuthenticationMeansFactory.CLIENT_TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.commerzbankgroup.common.authmeans.CommerzbankGroupAuthenticationMeansFactory.CLIENT_TRANSPORT_KEY_ID_NAME;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;

public class CommerzbankGroupSampleAuthenticationMeans {

    public static final UUID TRANSPORT_KEY_ID_SAMPLE = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static Map<String, BasicAuthenticationMean> getAuthenticationMeans() throws IOException, URISyntaxException {
        return Map.of(
                CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                        loadPemFile("yolt_certificate_transport.pem")),
                CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID_SAMPLE.toString()));
    }

    private static String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = CommerzbankGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
