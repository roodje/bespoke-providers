package com.yolt.providers.gruppocedacri;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
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
import static com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans.*;

@Getter
public class GruppoCedacriSampleTypedAuthenticationMeans {

    private final Map<String, BasicAuthenticationMean> authenticationMeans;

    @SneakyThrows
    public GruppoCedacriSampleTypedAuthenticationMeans() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "someClientId"));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "someClientSecret"));
        authenticationMeans.put(EMAIL_NAME, new BasicAuthenticationMean(CLIENT_EMAIL.getType(), "info@ttp.com"));
        authenticationMeans.put(CANCEL_LINK_NAME, new BasicAuthenticationMean(StringType.getInstance(), "https://www.test.com"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(),
                UUID.fromString("0aa0ceb4-9675-4649-a97a-138b7f843242").toString()));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile()));
    }

    private String loadPemFile() throws IOException, URISyntaxException {
        URI uri = GruppoCedacriSampleTypedAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/gruppocedacri/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
