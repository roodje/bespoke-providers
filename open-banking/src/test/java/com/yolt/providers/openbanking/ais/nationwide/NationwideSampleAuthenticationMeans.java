package com.yolt.providers.openbanking.ais.nationwide;

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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.*;

@Getter
public class NationwideSampleAuthenticationMeans {

    private static final String INSTITUTION_ID = "test";
    private static final String CLIENT_ID = "someClientId";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String SOFTWARE_STATEMENT_ASSERTION = "sample-software-statement-assertion";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public NationwideSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();

        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), "fake-organization-id"));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), "fake-software-id"));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION));

    }

    private String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = NationwideSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}