package com.yolt.providers.stet.cmarkeagroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.stet.cmarkeagroup.common.auth.CmArkeaGroupAuthenticationMeansSupplier.*;

@UtilityClass
public class CmArkeaGroupSampleMeans {

    private static final String SAMPLE_CLIENT_ID = "some_client_id";

    public static Map<String, BasicAuthenticationMean> createTestAuthenticationMeans() {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificates()));
        authenticationMeans.put(CLIENT_SIGNING_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), readCertificates()));
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), SAMPLE_CLIENT_ID));
        return authenticationMeans;
    }

    private static String readCertificates() {
        try {
            URI fileURI = CmArkeaGroupSampleMeans.class
                    .getClassLoader()
                    .getResource("certificates/fake-certificate.pem")
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }

    @SneakyThrows
    String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties) {
        Region region = properties.getRegions().get(0);
        AuthorizationRedirect authRedirect = AuthorizationRedirect.create(region.getAuthUrl());
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, authRedirect);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    public static String createAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String accessToken) {
        Region region = properties.getRegions().get(0);
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken);
        return objectMapper.writeValueAsString(providerState);
    }
}
