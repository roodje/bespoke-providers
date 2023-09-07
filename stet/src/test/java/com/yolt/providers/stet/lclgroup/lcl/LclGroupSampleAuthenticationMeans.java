package com.yolt.providers.stet.lclgroup.lcl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LclGroupSampleAuthenticationMeans {

    private static final String CLIENT_ID_NAME = "client-id";
    private static final String CLIENT_NAME_NAME = "client-name";
    private static final String CLIENT_CONTACT_EMAIL_NAME = "client-contact-email";
    private static final String CLIENT_TRANSPORT_KEY_ID_NAME = "private-client-transport-key-id";
    private static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    private static final String CLIENT_SIGNING_CERTIFICATE_NAME = "client-signing-certificate";
    private static final String CLIENT_SIGNING_KEY_ID_NAME = "private-client-signing-key-id";
    private static final String PROVIDER_LEGAL_ID_NAME = "provider-legal-id";

    private static final String HAPPY_CLIENT_ID = "client-id";
    private static final String UNHAPPY_CLIENT_ID = "unhappy-client-id";
    private static final String CLIENT_NAME = "Test Client";
    private static final String CLIENT_CONTACT_EMAIL = "contact-email@example.com";
    private static final String PROVIDER_LEGAL_ID = "PSDFR-ACPR-12345";
    private static final String SIGNING_KEY_ID = "0677504b-4c38-4c77-a50e-e979205f63ec";
    private static final String TRANSPORT_KEY_ID = "84413cd6-de73-4c55-9413-a730a68d2a55";
    private static final String CERTIFICATES_PATH = "certificates/fake-certificate.pem";

    public Map<String, BasicAuthenticationMean> getSampleAuthMeans() {
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>(getBasicAuthMeans());
        authMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), HAPPY_CLIENT_ID));
        return authMeans;
    }

    public Map<String, BasicAuthenticationMean> getSampleAuthMeansForUnhappyPisFLow() {
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>(getBasicAuthMeans());
        authMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), UNHAPPY_CLIENT_ID));
        return authMeans;
    }

    public Map<String, BasicAuthenticationMean> getAutoOnBoardingAuthMeans() {
        return getBasicAuthMeans();
    }

    @SneakyThrows
    static String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties) {
        Region region = properties.getRegions().get(0);
        AuthorizationRedirect authRedirect = AuthorizationRedirect.create(region.getAuthUrl());
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, authRedirect);
        return objectMapper.writeValueAsString(providerState);
    }

    private Map<String, BasicAuthenticationMean> getBasicAuthMeans() {
        String certificatePem = readFakeCertificate();

        return Map.of(CLIENT_NAME_NAME, new BasicAuthenticationMean(StringType.getInstance(), CLIENT_NAME),
                CLIENT_CONTACT_EMAIL_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_EMAIL.getType(), CLIENT_CONTACT_EMAIL),
                PROVIDER_LEGAL_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), PROVIDER_LEGAL_ID),
                CLIENT_SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), SIGNING_KEY_ID),
                CLIENT_TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), TRANSPORT_KEY_ID),
                CLIENT_SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), certificatePem),
                CLIENT_TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), certificatePem)
        );
    }

    private String readFakeCertificate() {
        try {
            URL certificateUrl = LclDataProviderV3RefreshAccessMeansRefreshTokenExpiredIntegrationTest.class.getClassLoader().getResource(CERTIFICATES_PATH);
            return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Error while reading local certificate", e);
        }
    }
}
