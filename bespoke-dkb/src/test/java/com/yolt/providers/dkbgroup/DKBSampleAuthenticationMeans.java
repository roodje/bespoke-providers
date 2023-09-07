package com.yolt.providers.dkbgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.PRIVATE_KEY_PEM;
import static com.yolt.providers.dkbgroup.common.auth.DKBGroupTypedAuthenticationMeansProducer.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.dkbgroup.common.auth.DKBGroupTypedAuthenticationMeansProducer.TRANSPORT_KEY_ID_NAME;

@Getter
public class DKBSampleAuthenticationMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public DKBSampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(PRIVATE_KEY_PEM.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
    }

    private String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
