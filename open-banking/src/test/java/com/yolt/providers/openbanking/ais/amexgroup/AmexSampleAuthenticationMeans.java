package com.yolt.providers.openbanking.ais.amexgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;

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
import static com.yolt.providers.openbanking.ais.amexgroup.common.auth.AmexAuthMeansBuilder.*;

public class AmexSampleAuthenticationMeans {

    public static Map<String, BasicAuthenticationMean> getAmexSampleAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "THE-CLIENT-ID"));
        authenticationMeans.put(CLIENT_SECRET, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "THE-CLIENT-SECRET"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, new BasicAuthenticationMean(KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        return authenticationMeans;
    }

    private static String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = RbsSampleAuthenticationMeansV4.class
                .getClassLoader()
                .getResource("certificates/fake/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}