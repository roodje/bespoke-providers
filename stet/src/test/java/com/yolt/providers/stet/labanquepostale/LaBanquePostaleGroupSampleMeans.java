package com.yolt.providers.stet.labanquepostale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeansSupplier.*;

@UtilityClass
class LaBanquePostaleGroupSampleMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate-chain.pem";
    private static final HashMap<String, BasicAuthenticationMean> PRECONFIGURED_AUTH_MEANS;
    private static final HashMap<String, BasicAuthenticationMean> CONFIGURED_AUTH_MEANS;

    static {
        Security.addProvider(new BouncyCastleProvider());

        PRECONFIGURED_AUTH_MEANS = new HashMap<>();
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "c18f46f1-350a-469a-9574-606c9ceacba4"));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_TRANSPORT_CERTIFICATE_CHAIN_NAME, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate()));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_EMAIL_ADDRESS_NAME, new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), "a@b.com"));
        PRECONFIGURED_AUTH_MEANS.put(PORTAL_USERNAME_NAME, new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), "user1"));
        PRECONFIGURED_AUTH_MEANS.put(PORTAL_PASSWORD_NAME, new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), "pass1"));
        PRECONFIGURED_AUTH_MEANS.put(CLIENT_NAME_NAME, new BasicAuthenticationMean(NoWhiteCharacterStringType.getInstance(), "ING Bank N.V."));

        CONFIGURED_AUTH_MEANS = new HashMap<>(PRECONFIGURED_AUTH_MEANS);
        CONFIGURED_AUTH_MEANS.put(CLIENT_ID_NAME, new BasicAuthenticationMean(API_KEY.getType(), "da5759d1-8f95-4b31-8183-fab1c62b52c5"));
        CONFIGURED_AUTH_MEANS.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(API_SECRET.getType(), "9bd412c1-58da-4be6-82dc-9809f43eeae9"));
    }

    Map<String, BasicAuthenticationMean> getPreconfiguredBasicAuthenticationMeans() {
        return PRECONFIGURED_AUTH_MEANS;
    }

    Map<String, BasicAuthenticationMean> getConfiguredAuthenticationMeans() {
        return CONFIGURED_AUTH_MEANS;
    }

    @SneakyThrows
    String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties) {
        Region region = properties.getRegions().get(0);
        AuthorizationRedirect authRedirect = AuthorizationRedirect.create(region.getAuthUrl());
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, authRedirect);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    String createAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String accessToken) {
        return createAuthorizedJsonProviderState(objectMapper, properties, accessToken, null);
    }

    @SneakyThrows
    String createAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String accessToken, String refreshToken) {
        Region region = properties.getRegions().get(0);
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken, refreshToken);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    String createPaymentJsonProviderState(ObjectMapper objectMapper, String paymentId) {
        PaymentProviderState providerState = PaymentProviderState.initiatedProviderState(paymentId);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    DataProviderState deserializeJsonProviderState(ObjectMapper objectMapper, String jsonProviderState) {
        return objectMapper.readValue(jsonProviderState, DataProviderState.class);
    }

    @SneakyThrows
    private static String readCertificate() {
        URL certificateUrl = LaBanquePostaleGroupTestConfig.class.getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }
}
