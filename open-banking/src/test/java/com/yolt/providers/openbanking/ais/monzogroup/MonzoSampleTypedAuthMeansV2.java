package com.yolt.providers.openbanking.ais.monzogroup;

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
import static com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper.*;

@Getter
public class MonzoSampleTypedAuthMeansV2 {

    private static final String INSTITUTION_ID_2 = "001580000103U9RAAU";
    private static final String CLIENT_ID_2 = "someClientId-2";
    private static final String CLIENT_SECRET_2 = "someClientSecret-2";
    private static final String SIGNING_KEY_HEADER_ID_2 = "signing-key-header-id-2";
    private static final String ORGANIZATION_ID_2 = "organization-id-2";
    private static final String SOFTWARE_ID_2 = "software-id-2";
    private static final String SIGNING_KEY_ID_2 = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID_2 = UUID.randomUUID().toString();
    private static final String SOFTWARE_STATEMENT_ASSERTION_2 = "sample-software-statement-assertion-2";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public MonzoSampleTypedAuthMeansV2() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID_2));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID_2));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID_2));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID_2));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID_2));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID_2));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID_2));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION_2));
    }

    private String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = MonzoSampleTypedAuthMeansV2.class
                .getClassLoader()
                .getResource("certificates/fake/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
