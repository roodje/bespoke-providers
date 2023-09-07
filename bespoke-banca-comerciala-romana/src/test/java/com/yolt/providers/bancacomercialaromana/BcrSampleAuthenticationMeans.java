package com.yolt.providers.bancacomercialaromana;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BcrSampleAuthenticationMeans {

    private static final String CLIENT_ID = "someClientId";
    private static final String CLIENT_SECRET = "someClientSecret";
    private static final String WEB_API_KEY = "beac2259-aa52-4225-9356-e3195120d43d";

    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    private static Map<String, BasicAuthenticationMean> getBcrSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));
        authenticationMeans.put(CLIENT_SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(PRIVATE_KEY_PEM.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(PRIVATE_KEY_PEM.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), loadPemFile("fake-certificate.pem")));
        authenticationMeans.put(WEB_API_KEY_NAME, new BasicAuthenticationMean(API_KEY_STRING.getType(), WEB_API_KEY));
        return authenticationMeans;
    }

    public static Map<String, BasicAuthenticationMean> getBcrSampleAuthenticationMeansForAis() throws IOException, URISyntaxException {
        return getBcrSampleAuthenticationMeans();
    }

    private static String loadPemFile(final String filename) throws IOException, URISyntaxException {
        URI uri = BcrSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/" + filename)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
