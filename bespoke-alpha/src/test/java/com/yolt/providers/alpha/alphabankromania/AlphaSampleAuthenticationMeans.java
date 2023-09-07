package com.yolt.providers.alpha.alphabankromania;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import lombok.Getter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.alpha.common.auth.AlphaTypedAuthenticationMeansProducer.*;
import static com.yolt.providers.common.constants.OAuth.CLIENT_ID;
import static com.yolt.providers.common.constants.OAuth.CLIENT_SECRET;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

@Getter
public class AlphaSampleAuthenticationMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";

    private final Map<String, BasicAuthenticationMean> authenticationMeans;

    public AlphaSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID_NAME));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(SUBSCRIPTION_KEY_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.ALIAS_STRING.getType(), SUBSCRIPTION_KEY_NAME));
    }

    private String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
