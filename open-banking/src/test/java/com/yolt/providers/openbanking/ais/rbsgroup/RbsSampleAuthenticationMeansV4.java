package com.yolt.providers.openbanking.ais.rbsgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

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
import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.*;

public class RbsSampleAuthenticationMeansV4 {

    private static final String INSTITUTION_ID = "test";
    private static final String CLIENT_ID = "someClientId";
    private static final String SIGNING_KEY_HEADER_ID = "someSigningKeyHeaderId";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String ORGANIZATION_ID = "someOrganizationId";
    private static final String SOFTWARE_ID = "someSoftwareId";
    private static final String FAKE_JWS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    private static Map<String, BasicAuthenticationMean> getRbsSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        //doubled
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATES_CHAIN_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATES_CHAIN_PEM.getType(), loadPemFile("fake-certificates-chain.pem")));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), FAKE_JWS));
        return authenticationMeans;
    }

    public static Map<String, BasicAuthenticationMean> getRbsSampleAuthenticationMeansForAis() throws IOException, URISyntaxException {
        return getRbsSampleAuthenticationMeans();
    }

    public static Map<String, BasicAuthenticationMean> getRbsSampleAuthenticationMeansForPis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeansForPis = new HashMap<>(getRbsSampleAuthenticationMeans());
        authenticationMeansForPis.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeansForPis.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
        return authenticationMeansForPis;
    }

    private static String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = RbsSampleAuthenticationMeansV4.class
                .getClassLoader()
                .getResource("certificates/fake/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
