package com.yolt.providers.fineco;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.Getter;

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
import static com.yolt.providers.fineco.auth.FinecoAuthenticationMeans.*;

@Getter
public class FinecoSampleTypedAuthenticationMeans {

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public FinecoSampleTypedAuthenticationMeans() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "clientId"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), "84719930-89c2-4008-8c7a-f09988131eff"));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile()));
    }

    private String loadPemFile() {
        try {
            URI uri = FinecoSampleTypedAuthenticationMeans.class
                    .getClassLoader()
                    .getResource("certificates/fake-certificate.pem")
                    .toURI();
            Path filePath = new File(uri).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
