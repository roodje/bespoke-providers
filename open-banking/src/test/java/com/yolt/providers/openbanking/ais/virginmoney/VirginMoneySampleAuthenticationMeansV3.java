package com.yolt.providers.openbanking.ais.virginmoney;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.*;

public class VirginMoneySampleAuthenticationMeansV3 {

    private static final String CERTIFICATE_PATH = "certificates/fake/fake-certificate.pem";


    private static final String INSTITUTION_ID_2 = "0016800001051XVBBZ";
    private static final String CLIENT_ID_2 = "someClientId-2";
    private static final String CLIENT_SECRET_2 = "someClientSecret-2";
    private static final String SIGNING_PRIVATE_KEY_ID_2 = "5b626fbf-9761-4dfb-a1d6-132f5ee40355";
    private static final String SIGNING_KEY_HEADER_ID_2 = "signing-key-header-id-2";
    private static final String TRANSPORT_PRIVATE_KEY_ID_2 = UUID.randomUUID().toString();
    private static final String ORGANIZATION_ID_2 = "organization-id-2";
    private static final String SOFTWARE_ID_2 = "software-id-2";
    private static final String SSA_2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    public Map<String, BasicAuthenticationMean> getVirginMoneySampleAuthenticationMeansForAutoonboarding() throws IOException, URISyntaxException {
        return getVirginMoneySampleAuthenticationMeansForAis();
    }

    public Map<String, BasicAuthenticationMean> getVirginMoneySampleAuthenticationMeansForAis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = getVirginMoneyCommonAuthenticationMeans();

        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SSA_2));

        return authenticationMeans;
    }

    public Map<String, BasicAuthenticationMean> getVirginMoneySampleAuthenticationMeansForPis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = getVirginMoneyCommonAuthenticationMeans();
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID_2));

        return authenticationMeans;
    }

    private Map<String, BasicAuthenticationMean> getVirginMoneyCommonAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID_2));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID_2));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET_2));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_PRIVATE_KEY_ID_2));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID_2));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_PRIVATE_KEY_ID_2));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID_2));

        return authenticationMeans;
    }

    public String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }

}