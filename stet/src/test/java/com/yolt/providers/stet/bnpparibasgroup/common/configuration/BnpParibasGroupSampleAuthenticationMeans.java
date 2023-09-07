package com.yolt.providers.stet.bnpparibasgroup.common.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.AuthenticationMeanType;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.stet.bnpparibasgroup.common.BnpParibasGroupTestsConstants;
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

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.bnpparibasgroup.common.auth.BnpParibasGroupAuthenticationMeansSupplier.CLIENT_REGISTRATION_ACCESS_TOKEN_NAME;
import static com.yolt.providers.stet.bnpparibasgroup.common.auth.BnpParibasGroupAuthenticationMeansSupplier.CLIENT_REGISTRATION_ACCESS_TOKEN_TYPE;

public class BnpParibasGroupSampleAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_CONTACT_EMAIL = "client-contact-email";
    public static final String CLIENT_WEBSITE_URI = "client-website-uri";
    public static final String CLIENT_LOGO_URI = "client-logo-uri";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME_V2 = "client-transport-certificate-v2";
    public static final String CLIENT_SIGNING_CERTIFICATE_NAME_V2 = "client-signing-certificate-v2";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME_V2 = "client-transport-private-keyid-v2";
    public static final String CLIENT_SIGNING_KEY_ID_NAME_V2 = "client-signing-private-keyid-v2";

    public Map<String, BasicAuthenticationMean> getBnpSampleAuthenticationMeans() throws IOException, URISyntaxException {
        String certificate = readFakeCertificatePem();
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        authMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "client-id"));
        authMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "client-secret"));
        authMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME_V2, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), certificate));
        authMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME_V2, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), certificate));
        authMeans.put(CLIENT_SIGNING_KEY_ID_NAME_V2, new BasicAuthenticationMean(KEY_ID.getType(), "0677504b-4c38-4c77-a50e-e979205f63ec"));
        authMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME_V2, new BasicAuthenticationMean(KEY_ID.getType(), "84413cd6-de73-4c55-9413-a730a68d2a55"));
        authMeans.put(CLIENT_CONTACT_EMAIL, new BasicAuthenticationMean(CLIENT_EMAIL.getType(), "contact-email@example.com"));
        authMeans.put(CLIENT_WEBSITE_URI, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Client website uri (URL encoded)"), "https://www.example.com/website"));
        authMeans.put(CLIENT_LOGO_URI, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Client logo uri (URL encoded)"), "https://www.example.com/logo"));
        authMeans.put(CLIENT_REGISTRATION_ACCESS_TOKEN_NAME, new BasicAuthenticationMean(CLIENT_REGISTRATION_ACCESS_TOKEN_TYPE.getType(), "87e7c450-a87b-4b2a-a512-37c8c3fc998a"));
        return authMeans;
    }

    private AuthenticationMeanType getCustomizedAuthenticationMeanType(String displayName) {
        return new TypedAuthenticationMeans(displayName, NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING).getType();
    }

    private String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(BnpParibasGroupTestsConstants.CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }

    @SneakyThrows
    public static String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties) {
        Region region = properties.getRegions().get(0);
        AuthorizationRedirect authRedirect = AuthorizationRedirect.create(region.getAuthUrl());
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, authRedirect);
        return objectMapper.writeValueAsString(providerState);
    }
}
