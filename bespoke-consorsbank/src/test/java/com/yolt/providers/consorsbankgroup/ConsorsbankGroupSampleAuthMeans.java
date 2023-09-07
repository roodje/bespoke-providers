package com.yolt.providers.consorsbankgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthenticationMeans;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class ConsorsbankGroupSampleAuthMeans {

    public static String TEST_TRANSPORT_KEY_ID = UUID.randomUUID().toString();


    public static Map<String, BasicAuthenticationMean> sampleAuthMeans() throws IOException, URISyntaxException {
        return Map.ofEntries(
                Map.entry(
                        DefaultAuthenticationMeans.CLIENT_TRANSPORT_KEY_ID_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), TEST_TRANSPORT_KEY_ID)),
                Map.entry(
                        DefaultAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_NAME,
                        new BasicAuthenticationMean(TypedAuthenticationMeans.CERTIFICATE_PEM.getType(), loadPemFile()))
        );
    }

    private static String loadPemFile() throws IOException, URISyntaxException {
        URI uri = ConsorsbankGroupSampleAuthMeans.class
                .getClassLoader()
                .getResource("certificates/consorsbankgroup/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

}
