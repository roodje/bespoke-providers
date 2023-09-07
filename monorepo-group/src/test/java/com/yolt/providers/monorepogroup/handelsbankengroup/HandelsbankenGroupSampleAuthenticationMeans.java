package com.yolt.providers.monorepogroup.handelsbankengroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeansProducerV1.*;

@UtilityClass
class HandelsbankenGroupSampleAuthenticationMeans {

    Map<String, BasicAuthenticationMean> preOnboardingAuthMeans() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), getCert()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "efabcd2e-c84d-4e19-ae46-79ece27681d0"));
        authenticationMeans.put(APPLICATION_NAME_NAME, new BasicAuthenticationMean(StringType.getInstance(), "Yolt"));
        authenticationMeans.put(APPLICATION_DESCRIPTION_NAME, new BasicAuthenticationMean(StringType.getInstance(), "Test description"));
        return authenticationMeans;
    }

    Map<String, BasicAuthenticationMean> postOnboardingAuthMeans() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), getCert()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "efabcd2e-c84d-4e19-ae46-79ece27681d0"));
        authenticationMeans.put(APPLICATION_NAME_NAME, new BasicAuthenticationMean(StringType.getInstance(), "Yolt"));
        authenticationMeans.put(APPLICATION_DESCRIPTION_NAME, new BasicAuthenticationMean(StringType.getInstance(), "Test description"));
        authenticationMeans.put(TPP_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "SE-FINA-100001"));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "2cffdb50-2323-4be7-a2a2-70a6610f8a06"));
        return authenticationMeans;
    }

    @SneakyThrows
    private String getCert() {
        URI uri = HandelsbankenGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource("certificates/fake-certificate.pem")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
