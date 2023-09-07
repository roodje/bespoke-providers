package com.yolt.providers.openbanking.ais.tsbgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
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

@Getter
public class TsbGroupSampleTypedAuthenticationMeans {

    private static final String INSTITUTION_ID = "003882287203T83JKK";
    private static final String CLIENT_ID = "someClientId";
    private static final String CLIENT_SECRET = "someClientSecret";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String ORGANIZATION_ID = "organization-id";
    private static final String SOFTWARE_ID = "software-id";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String SOFTWARE_STATEMENT_ASSERTION = "sample-software-statement-assertion";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public TsbGroupSampleTypedAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(TsbGroupAuthMeansBuilderV3.SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION));
    }

    public static Map<String, BasicAuthenticationMean> getTsbGroupSampleTypedAuthenticationMeansForFcaRegistration() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeansForRegister = new HashMap<>();
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("fake-certificate.pem")));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeansForRegister.put(TsbGroupAuthMeansBuilderV3.SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION));
        return authenticationMeansForRegister;
    }

    private static String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = TsbGroupSampleTypedAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
