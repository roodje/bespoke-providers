package com.yolt.providers.openbanking.ais.vanquisgroup;

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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2.*;

@Getter
public class VanquisGroupSampleTypedAuthenticationMeansV2 {

    private static final String INSTITUTION_ID = "test";
    private static final String CLIENT_ID = "someClientId";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String SOFTWARE_ID = "sample-software-id";
    ;

    public static Map<String, BasicAuthenticationMean> getAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "5b626fbf-9761-4dfb-a1d6-132f5ee40355"));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
        authenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, new BasicAuthenticationMean(SOFTWARE_STATEMENT_ASSERTION_STRING.getType(), "fake"));
        return authenticationMeans;

    }

    private static String loadPemFile(final String fileName) throws IOException, URISyntaxException {
        URI uri = VanquisGroupSampleTypedAuthenticationMeansV2.class
                .getClassLoader()
                .getResource("certificates/fake/" + fileName)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
