package com.yolt.providers.monorepogroup.libragroup.common;

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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.*;

@Getter
public class LibraGroupSampleAuthenticationMeans {

    public static final String CLIENT_ID_SAMPLE = "clientId";
    public static final String CLIENT_SECRET_SAMPLE = "clientSecret";
    public static final UUID SIGNING_KEY_ID_SAMPLE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public Map<String, BasicAuthenticationMean> authenticationMeans;

    public LibraGroupSampleAuthenticationMeans() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), CLIENT_ID_SAMPLE));
        authenticationMeans.put(CLIENT_SECRET, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SECRET_STRING.getType(), CLIENT_SECRET_SAMPLE));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadSigningCertificate()));
        authenticationMeans.put(SIGNING_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID_SAMPLE.toString()));
    }

    private String loadSigningCertificate() {
        try {
            URI uri = LibraGroupSampleAuthenticationMeans.class
                    .getClassLoader()
                    .getResource("certificates/fake-certificate.pem")
                    .toURI();
            Path filePath = new File(uri).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}