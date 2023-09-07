package com.yolt.providers.kbcgroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans.CLIENT_TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans.CLIENT_TRANSPORT_KEY_ID_NAME;

@UtilityClass
public class KbcGroupSampleAuthenticationMeans {

    @SneakyThrows
    public Map<String, BasicAuthenticationMean> get() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "dddc83c5-2482-48f0-ab10-025d6d3ccd30"));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(),
                loadPemFile()));
        return authenticationMeans;
    }

    private String loadPemFile() throws IOException, URISyntaxException {
        URI uri = KbcGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/kbcgroup/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
