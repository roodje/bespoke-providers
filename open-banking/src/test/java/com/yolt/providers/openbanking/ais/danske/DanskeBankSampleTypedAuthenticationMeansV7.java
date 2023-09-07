package com.yolt.providers.openbanking.ais.danske;

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
import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.*;

@Getter
public class DanskeBankSampleTypedAuthenticationMeansV7 {

    private static final String TRANSPORT_CERTIFICATE_FILE = "fake-certificate.pem";

    private static final String SOFTWARE_ID = "softID";
    private static final String SOFTWARE_STATEMENT_ASSERTION = "sample-software-statement-assertion";
    private static final String INSTITUTION_ID = "0015800000jf7AeAAI";
    private static final String CLIENT_ID = "2892ebea7ea9befa778897b5454fea56fb564fba4ebf65e4ba6546789fae98a9";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String ORGANIZATION_ID = "sample-organization-id";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public DanskeBankSampleTypedAuthenticationMeansV7() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION));
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadTransportCertificatePem()));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
    }

    private String loadTransportCertificatePem() {
        try {
            URI uri = DanskeBankSampleTypedAuthenticationMeansV7.class
                    .getClassLoader()
                    .getResource("certificates/fake/" + TRANSPORT_CERTIFICATE_FILE)
                    .toURI();
            Path filePath = new File(uri).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Could not load transport certificate");
        }
    }
}