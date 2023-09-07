package com.yolt.providers.stet.bpcegroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.stet.bpcegroup.common.auth.BpceAuthenticationMeansSupplier.*;

public class BpceGroupSampleAuthenticationMeans {

    private final Map<String, BasicAuthenticationMean> means;

    public BpceGroupSampleAuthenticationMeans() {
        means = new HashMap<>();
        means.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "THE_CLIENT_ID"));
        means.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate("certificates/bpce/yolt_certificate.pem")));
        means.put(CLIENT_SIGNING_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
    }

    public Map<String, BasicAuthenticationMean> getBasicAuthenticationMeans() {
        return means;
    }

    public Map<String, BasicAuthenticationMean> getPreRegistrationAuthenticationMeans() {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate("certificates/bpce/yolt_certificate.pem")));
        means.put(CLIENT_SIGNING_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
        means.put(CLIENT_SIGNING_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), readCertificate("certificates/bpce/yolt_certificate_signing.pem")));
        means.put(CLIENT_EMAIL, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_EMAIL.getType(), "connections@acme.com"));
        means.put(CLIENT_NAME, new BasicAuthenticationMean(CLIENT_NAME_TYPE.getType(), "Acme Co."));
        means.put(CLIENT_PHONE_NUMBER, new BasicAuthenticationMean(CLIENT_PHONE_NUMBER_TYPE.getType(), "+48123456789"));
        return means;
    }

    public Map<String, BasicAuthenticationMean> getPostRegistrationAuthenticationMeans() {
        Map<String, BasicAuthenticationMean> means = new HashMap<>();
        means.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        means.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate("certificates/bpce/yolt_certificate.pem")));
        means.put(CLIENT_SIGNING_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), "11111111-1111-1111-1111-111111111111"));
        means.put(CLIENT_SIGNING_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), readCertificate("certificates/bpce/yolt_certificate_signing.pem")));
        means.put(CLIENT_EMAIL, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_EMAIL.getType(), "connections@acme.com"));
        means.put(CLIENT_NAME, new BasicAuthenticationMean(CLIENT_NAME_TYPE.getType(), "Acme Co."));
        means.put(CLIENT_PHONE_NUMBER, new BasicAuthenticationMean(CLIENT_PHONE_NUMBER_TYPE.getType(), "+48987654321"));
        means.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "1PSDNL-DNB-33031431-0-RWMH3"));
        return means;
    }

    @SneakyThrows
    public String createEmptyJsonProviderState(ObjectMapper objectMapper) {
        return objectMapper.writeValueAsString(DataProviderState.emptyState());
    }

    @SneakyThrows
    String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, Region region) {
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, null);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    DataProviderState createProviderState(ObjectMapper objectMapper, String jsonProviderState) {
        return objectMapper.readValue(jsonProviderState, DataProviderState.class);
    }

    @SneakyThrows
    String createAuthorizedJsonProviderState(ObjectMapper objectMapper, Region region, String accessToken, String refreshToken) {
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken, refreshToken);
        return objectMapper.writeValueAsString(providerState);
    }

    private String readCertificate(String file) {
        try {
            URI fileURI = BpceGroupSampleAuthenticationMeans.class
                    .getClassLoader()
                    .getResource(file)
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
