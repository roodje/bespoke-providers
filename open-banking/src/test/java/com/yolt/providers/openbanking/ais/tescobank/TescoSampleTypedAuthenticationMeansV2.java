package com.yolt.providers.openbanking.ais.tescobank;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
import static com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TescoSampleTypedAuthenticationMeansV2 {

    private static final String INSTITUTION_ID = "0017440000hdQV5CCT";
    private static final String CLIENT_ID = "someClientId";
    private static final String ORGANIZATION_NAME = "organization_name";
    private static final String SOFTWARE_NAME = "software_name";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String SOFTWARE_STATEMENT_ASSERTION = "sample-software-statement-assertion";


    public static Map<String, BasicAuthenticationMean> getTypedAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans;

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_NAME));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_NAME));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SOFTWARE_STATEMENT_ASSERTION));

        return authenticationMeans;
    }

    private static String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = TescoSampleTypedAuthenticationMeansV2.class
                .getClassLoader()
                .getResource("certificates/fake/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
