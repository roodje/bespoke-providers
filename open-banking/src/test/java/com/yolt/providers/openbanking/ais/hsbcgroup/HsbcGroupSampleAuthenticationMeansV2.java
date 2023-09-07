package com.yolt.providers.openbanking.ais.hsbcgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.*;

public class HsbcGroupSampleAuthenticationMeansV2 {

    private static final String CERTIFICATE_PATH = "certificates/fake/fake-certificate.pem";

    private static final String INSTITUTION_ID = "00162010018e22KCTR";
    private static final String CLIENT_ID = "c54976c8-71a7-4e53-b3a5-b68260698d5e";
    private static final String SIGNING_KEY_HEADER_ID = "someSigningKeyHeaderId";
    private static final String SIGNING_PRIVATE_KEY_ID = "5b626fbf-9761-4dfb-a1d6-132f5ee40355";
    private static final String TRANSPORT_PRIVATE_KEY_ID = "2d4492f7-0188-4cbb-bd0c-c92c034b5cf7";
    private static final String ORGANIZATION_ID = "someOrganizationId";
    private static final String SOFTWARE_ID = "someSoftwareId";

    public Map<String, BasicAuthenticationMean> getHsbcGroupSampleAuthenticationMeansForAis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = getHsbcGroupCommonAuthenticationMeans();

        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), "fake"));

        return authenticationMeans;
    }

    public Map<String, BasicAuthenticationMean> getHsbcGroupSampleAuthenticationMeansForPis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = getHsbcGroupCommonAuthenticationMeans();

        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));

        return authenticationMeans;
    }

    public String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }

    private Map<String, BasicAuthenticationMean> getHsbcGroupCommonAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();

        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_PRIVATE_KEY_ID));
        authenticationMeans.put(PRIVATE_SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_PRIVATE_KEY_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));

        return authenticationMeans;
    }
}