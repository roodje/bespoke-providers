package com.yolt.providers.cbiglobe;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

@Getter
public class CbiGlobeSampleTypedAuthenticationMeans {

    private static final String TRANSPORT_KEY_ID = "2be4d475-f240-42c7-a22c-882566ac0f95";
    private static final String SIGNING_KEY_ID = "2e9ecac7-b840-4628-8036-d4998dfb8959";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @SneakyThrows
    public CbiGlobeSampleTypedAuthenticationMeans() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authenticationMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "fakepaymentclientid"));
        authenticationMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(TPP_ID.getType(), "fakepaymentclientsecret"));
    }

    private static String readCertificate() throws IOException, URISyntaxException {
        URL resource = CbiGlobeAuthenticationMeans.class
                .getClassLoader().getResource("certificates/fake-certificate.pem");

        Path filePath = new File(Objects.requireNonNull(resource).toURI()).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
