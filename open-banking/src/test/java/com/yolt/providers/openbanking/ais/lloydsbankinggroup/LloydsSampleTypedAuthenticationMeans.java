package com.yolt.providers.openbanking.ais.lloydsbankinggroup;

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
import static com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3.*;

@Getter
public class LloydsSampleTypedAuthenticationMeans {

    private static final String INSTITUTION_ID = "1238972887jdsIWNND";
    private static final String CLIENT_ID = "a4f99159-cb97-4667-b82e-553e8ad8a632";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String AIS_SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String AIS_TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String PIS_SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String PIS_TRANSPORT_KEY_ID = UUID.randomUUID().toString();

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public LloydsSampleTypedAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();

        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(ORGANIZATION_ID_NAME, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), "fake-organization"));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), "fake-software-id"));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), AIS_TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), AIS_SIGNING_KEY_ID));
    }

    private String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = LloydsSampleTypedAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
