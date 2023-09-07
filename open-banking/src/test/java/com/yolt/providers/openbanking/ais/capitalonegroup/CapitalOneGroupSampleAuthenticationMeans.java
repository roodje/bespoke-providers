package com.yolt.providers.openbanking.ais.capitalonegroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.Getter;
import lombok.SneakyThrows;

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
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3.REGISTRATION_ACCESS_TOKEN_STRING;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3.*;

@Getter
public class CapitalOneGroupSampleAuthenticationMeans {

    private static final String INSTITUTION_ID = "TEST_INSTITUTION_ID";
    private static final String SOFTWARE_ID = "TEST_SOFTWARE_ID";
    private static final String CLIENT_ID = "TEST_CLIENT_ID";
    private static final String CLIENT_SECRET = "TEST_CLIENT_SECRET";
    private static final String SIGNING_KEY_HEADER_ID = "TEST_SIGNING_KEY_HEADER_ID";
    private static final String SIGNING_PRIVATE_KEY_ID = "5b626fbf-9761-4dfb-a1d6-132f5ee40123";
    private static final UUID TRANSPORT_PRIVATE_KEY_ID = UUID.randomUUID();
    private static final String SSA = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String REGISTRATION_ACCESS_TOKEN = "REGISTRATION_ACCESS_TOKEN";

    @SneakyThrows
    public static Map<String, BasicAuthenticationMean> getSampleAuthenticationMeans() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_PRIVATE_KEY_ID));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_PRIVATE_KEY_ID.toString()));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), SSA));
        authenticationMeans.put(REGISTRATION_ACCESS_TOKEN_NAME, new BasicAuthenticationMean(REGISTRATION_ACCESS_TOKEN_STRING.getType(), REGISTRATION_ACCESS_TOKEN));

        return authenticationMeans;
    }

    private static String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = CapitalOneGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}