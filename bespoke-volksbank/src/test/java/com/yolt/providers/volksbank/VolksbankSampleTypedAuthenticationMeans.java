package com.yolt.providers.volksbank;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans.*;

@Getter
public class VolksbankSampleTypedAuthenticationMeans {

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @SneakyThrows
    public VolksbankSampleTypedAuthenticationMeans() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "someClientId"));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "someClientSecret"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile()));
    }

    private String loadPemFile() throws IOException, URISyntaxException {
        URI uri = VolksbankSampleTypedAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/volksbank/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
