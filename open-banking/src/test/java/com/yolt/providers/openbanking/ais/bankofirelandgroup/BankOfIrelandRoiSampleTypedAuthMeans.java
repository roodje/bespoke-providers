package com.yolt.providers.openbanking.ais.bankofirelandgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi.auth.BankOfIrelandRoiAuthMeansMapper.*;

public class BankOfIrelandRoiSampleTypedAuthMeans {

    private static final String CERTIFICATE_CHAIN_PATH = "certificates/fake/fake-certificates-chain.pem";

    private static final String SOFTWARE_ID = "softId";
    private static final String INSTITUTION_ID = "0011800000tfV9aBBE";
    private static final String SOFTWARE_STATEMENT_ASSERTION = "sample-software-statement-assertion";
    private static final String CLIENT_ID = "someClientId";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();

    public static Map<String, BasicAuthenticationMean> getSampleAuthMeans() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_CHAIN_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATES_CHAIN_PEM.getType(),
                readFakeCertificatePem()));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        return authenticationMeans;
    }

    public static String readFakeCertificatePem() {
        URL certificateUrl = BankOfIrelandRoiSampleTypedAuthMeans.class.getClassLoader().getResource(CERTIFICATE_CHAIN_PATH);
        try {
            return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't prepare sample auth means");
        }
    }
}