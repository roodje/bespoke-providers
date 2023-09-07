package com.yolt.providers.rabobank;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.*;

public class RabobankSampleTypedAuthenticationMeans {

    private static final String CERTIFICATE_PATH = "certificates/rabobank_certificate.pem";

    public Map<String, BasicAuthenticationMean> getRabobankSampleTypedAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();

        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(API_KEY.getType(), "0b7df55a-8f51-454e-9de8-87b9cac2aba4"));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(API_SECRET.getType(), "0b7df55a-8f51-454e-9de8-87b9cac2aba4"));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(CLIENT_SIGNING_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "0677504b-4c38-4c77-a50e-e979205f63ec"));
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), "84413cd6-de73-4c55-9413-a730a68d2a55"));

        return authenticationMeans;
    }

    public String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}