package com.yolt.providers.openbanking.ais.aibgroup;

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
import static com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.ORGANIZATION_ID_NAME;
import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.SOFTWARE_ID_NAME;

@Getter
public class AibGroupSampleAuthenticationMeans {

    private static final String INSTITUTION_ID = "0015800000jf9VgAAI";
    private static final String CLIENT_ID = "someClientId";
    private static final String CLIENT_SECRET = "someClientSecret";
    private static final String SIGNING_KEY_HEADER_ID = "someSigningKeyHeaderId";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String SOFTWARE_ID = "someSoftwareId";
    private static final String ORGANIZATION_ID = "someOrganizationId";
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    private static Map<String, BasicAuthenticationMean> getAibGroupSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        return authenticationMeans;
    }

    public static Map<String, BasicAuthenticationMean> getAibGroupSampleAuthenticationMeansForAis() throws IOException, URISyntaxException {
        return getAibGroupSampleAuthenticationMeans();
    }

    public static Map<String, BasicAuthenticationMean> getAibGroupSampleAuthenticationMeansForPis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeansForPis = new HashMap<>(getAibGroupSampleAuthenticationMeans());
        authenticationMeansForPis.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeansForPis.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
        return authenticationMeansForPis;
    }

    private static String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = AibGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}

