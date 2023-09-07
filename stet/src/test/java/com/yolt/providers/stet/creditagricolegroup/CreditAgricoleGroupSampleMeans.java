package com.yolt.providers.stet.creditagricolegroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.domain.CreditAgricoleAccessMeansDTO;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.domain.CreditAgricoleRegion;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.labanquepostale.LaBanquePostaleGroupTestConfig;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.creditagricolegroup.creditagricole.auth.CreditAgricoleAuthenticationMeansSupplier.*;

@UtilityClass
class CreditAgricoleGroupSampleMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final HashMap<String, BasicAuthenticationMean> PRECONFIGURED_AUTH_MEANS;
    private static final HashMap<String, BasicAuthenticationMean> CONFIGURED_AUTH_MEANS;

    static {
        PRECONFIGURED_AUTH_MEANS = new HashMap<>();
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "c18f46f1-350a-469a-9574-606c9ceacba4"));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate()));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "c18f46f1-350a-469a-9574-606c9ceacba4"));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate()));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_EMAIL_NAME, new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), "test@example.com"));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_NAME, new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), "Test client"));

        CONFIGURED_AUTH_MEANS = new HashMap<>(PRECONFIGURED_AUTH_MEANS);
        CONFIGURED_AUTH_MEANS.put(CLIENT_ID_NAME, new BasicAuthenticationMean(API_KEY.getType(), "f89a65f5-b4b4-4ef3-8bb3-3697ebb9ce04"));
    }

    Map<String, BasicAuthenticationMean> getPreconfiguredBasicAuthenticationMeans() {
        return PRECONFIGURED_AUTH_MEANS;
    }

    Map<String, BasicAuthenticationMean> getConfiguredAuthenticationMeans() {
        return CONFIGURED_AUTH_MEANS;
    }

    Map<String, BasicAuthenticationMean> getConfiguredAuthenticationMeans(String contactEmail) {
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>(CONFIGURED_AUTH_MEANS);
        authMeans.put(CLIENT_EMAIL_NAME,  new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), contactEmail));
        return authMeans;
    }

    @SneakyThrows
    String createEmptyJsonProviderState(ObjectMapper objectMapper) {
        DataProviderState providerState = DataProviderState.emptyState();
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, Region region) {
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, null);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    String createAuthorizedJsonProviderState(ObjectMapper objectMapper, Region region, String accessToken) {
        return createAuthorizedJsonProviderState(objectMapper, region, accessToken, null);
    }

    @SneakyThrows
    String createAuthorizedJsonProviderState(ObjectMapper objectMapper, Region region, String accessToken, String refreshToken) {
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken, refreshToken);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    String createOldJsonProviderState(ObjectMapper objectMapper, CreditAgricoleRegion region, String accessToken, String refreshToken) {
        CreditAgricoleAccessMeansDTO accessMeans = new CreditAgricoleAccessMeansDTO();
        accessMeans.setRegion(region);
        accessMeans.setAccessToken(accessToken);
        accessMeans.setRefreshToken(refreshToken);
        accessMeans.setExpiresIn(10);
        return objectMapper.writeValueAsString(accessMeans);
    }

    @SneakyThrows
    DataProviderState createProviderState(ObjectMapper objectMapper, String jsonProviderState) {
        return objectMapper.readValue(jsonProviderState, DataProviderState.class);
    }

    @SneakyThrows
    private static String readCertificate() {
        URL certificateUrl = LaBanquePostaleGroupTestConfig.class.getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
