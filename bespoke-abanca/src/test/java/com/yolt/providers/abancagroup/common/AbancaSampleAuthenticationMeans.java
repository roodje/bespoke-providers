package com.yolt.providers.abancagroup.common;

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
import java.util.UUID;

import static com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans.API_KEY;
import static com.yolt.providers.abancagroup.common.ais.auth.AbancaAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

@Getter
public class AbancaSampleAuthenticationMeans {

    public static final UUID CLIENT_ID_SAMPLE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID TRANSPORT_KEY_ID_SAMPLE = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID SIGNING_KEY_ID_SAMPLE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID API_KEY_SAMPLE = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public Map<String, BasicAuthenticationMean> authenticationMeans;

    public AbancaSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_UUID.getType(), CLIENT_ID_SAMPLE.toString()));
        authenticationMeans.put(API_KEY, new BasicAuthenticationMean(API_KEY_STRING.getType(), API_KEY_SAMPLE.toString()));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_signing.cer")));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("example_client_tls.cer")));
        authenticationMeans.put(TRANSPORT_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID_SAMPLE.toString()));
        authenticationMeans.put(SIGNING_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID_SAMPLE.toString()));
    }

    private String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = AbancaSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}