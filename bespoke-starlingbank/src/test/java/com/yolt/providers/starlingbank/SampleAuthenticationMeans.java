package com.yolt.providers.starlingbank;

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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans.*;

@Getter
public class SampleAuthenticationMeans {

    private static final String API_KEY = "api-key";
    private static final String API_SECRET = "api-secret";
    private static final String SIGNING_KEY_HEADER_ID = "aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaaaaaa";
    private static final String SIGNING_PRIVATE_KEY_ID = "c2724630-af13-11ea-b3de-0242ac130004";
    private static final String TRANSPORT_KEY_ID = "e9e17e04-a3de-4b34-8008-0e3e2bfdefa1";
    private static final String CERTIFICATE_PATH = "starlingbank/certificates/fake-certificate.pem";


    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public SampleAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(API_KEY_NAME_2, new BasicAuthenticationMean(API_KEY_STRING.getType(), API_KEY));
        authenticationMeans.put(API_SECRET_NAME_2, new BasicAuthenticationMean(API_SECRET_STRING.getType(), API_SECRET));
        authenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME_2, new BasicAuthenticationMean(KEY_ID_HEADER_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME_2, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_PRIVATE_KEY_ID));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME_2, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME_2, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
    }

    private String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
