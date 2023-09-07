package com.yolt.providers.openbanking.ais.revolutgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutGbAuthMeansBuilderV2;
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
import static com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutEuAuthMeansBuilderV2.*;

@Getter
public class RevolutSampleAuthenticationMeans {

    public static final String TEST_SIGNING_KEY_ID = UUID.randomUUID().toString();
    public static final String TEST_TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    public static final String TEST_SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    public static final String TEST_CLIENT_ID = "06951f38-6aed-4005-a54e-f039f978f119";
    public static final String TEST_INSTITUTION_ID = "0032400006451CCVVX";
    public static final String TEST_JWKS_ENDPOINT = "https://keystore.openbanking.org.uk/organizationId/softwareId.jwks";
    public static final String TEST_ORG_NAME = "TPP Org Name";
    public static final String TEST_SOFTWARE_CLIENT_NAME = "TPP Name";
    public static final String TEST_SOFTWARE_ID = "testSoftwareId";
    public static final String TEST_ORGANIZATION_ID = "testOrganizationId";
    public static final String TEST_SSA = "testSsa";

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Map<String, BasicAuthenticationMean> authenticationMeansForEidasRegistration;
    private Map<String, BasicAuthenticationMean> authenticationMeansNewOBCerts;


    public RevolutSampleAuthenticationMeans() throws Exception {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), TEST_CLIENT_ID));
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), TEST_INSTITUTION_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), TEST_SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TEST_TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TEST_SIGNING_KEY_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));

        authenticationMeansForEidasRegistration = new HashMap<>(authenticationMeans);
        authenticationMeansForEidasRegistration.put(ORG_JWKS_ENDPOINT_NAME, new BasicAuthenticationMean(ORG_JWKS_ENDPOINT_TYPE.getType(), TEST_JWKS_ENDPOINT));
        authenticationMeansForEidasRegistration.put(ORG_NAME_NAME, new BasicAuthenticationMean(ORG_NAME_TYPE.getType(), TEST_ORG_NAME));
        authenticationMeansForEidasRegistration.put(SOFTWARE_CLIENT_NAME_NAME, new BasicAuthenticationMean(SOFTWARE_CLIENT_NAME_TYPE.getType(), TEST_SOFTWARE_CLIENT_NAME));

        authenticationMeansNewOBCerts = new HashMap<>();
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), TEST_CLIENT_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), TEST_INSTITUTION_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), TEST_SIGNING_KEY_HEADER_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), TEST_SOFTWARE_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), TEST_ORGANIZATION_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TEST_TRANSPORT_KEY_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TEST_SIGNING_KEY_ID));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeansNewOBCerts.put(RevolutGbAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), TEST_SSA));

        authenticationMeans.putAll(authenticationMeansNewOBCerts);
    }

    private String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = RevolutSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

}
