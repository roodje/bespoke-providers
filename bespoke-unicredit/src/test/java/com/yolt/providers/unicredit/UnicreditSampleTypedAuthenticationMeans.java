package com.yolt.providers.unicredit;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.*;

@Getter
public
class UnicreditSampleTypedAuthenticationMeans {

    private static final String AVAILABLE_KEY_ID = UUID.randomUUID().toString();
    private static final String CLIENT_EMAIL_VALUE = "user@example.com";

    private Map<String, BasicAuthenticationMean> authMeans;

    public UnicreditSampleTypedAuthenticationMeans(final String certPath) throws IOException, URISyntaxException {
        authMeans = new HashMap<>();
        authMeans.put(EIDAS_CERTIFICATE, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readFile(certPath)));
        authMeans.put(EIDAS_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), AVAILABLE_KEY_ID));
        authMeans.put(CLIENT_EMAIL, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_EMAIL.getType(), CLIENT_EMAIL_VALUE));
    }

    private static String readFile(final String filename) throws IOException, URISyntaxException {
        URI fileURI = UnicreditSampleTypedAuthenticationMeans.class
                .getClassLoader()
                .getResource(filename)
                .toURI();
        Path filePath = new File(fileURI).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}