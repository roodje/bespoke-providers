package com.yolt.providers.openbanking.ais.barclaysgroup;

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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.barclaysgroup.common.auth.BarclaysGroupAuthMeansBuilderV3.*;

@Getter
public class BarclaysSampleTypedAuthenticationMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake/fake-certificate.pem";

    private static final String INSTITUTION_ID = "0017440000hdQV5CCT";
    private static final String CLIENT_ID = "someClientId";
    private static final String SIGNING_KEY_HEADER_ID = "signing-key-header-id";
    private static final String ORGANIZATION_ID = "some-organization-id";
    private static final String SOFTWARE_ID = "some-software-id";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();

    private Map<String, BasicAuthenticationMean> authenticationMean;

    public BarclaysSampleTypedAuthenticationMeans() throws IOException, URISyntaxException {
        authenticationMean = new HashMap<>();
        authenticationMean.put(CLIENT_ID_NAME_V2, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMean.put(INSTITUTION_ID_NAME_V2, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMean.put(PRIVATE_SIGNING_KEY_HEADER_ID_NAME_V2, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), SIGNING_KEY_HEADER_ID));
        authenticationMean.put(TRANSPORT_CERTIFICATE_NAME_V2, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMean.put(ORGANIZATION_ID_NAME_V2, new BasicAuthenticationMean(ORGANIZATION_ID_STRING.getType(), ORGANIZATION_ID));
        authenticationMean.put(SOFTWARE_ID_NAME_V2, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMean.put(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMean.put(SIGNING_PRIVATE_KEY_ID_NAME_V2, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
    }

    private String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}