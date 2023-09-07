package com.yolt.providers.stet.cicgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.cicgroup.common.auth.CicGroupAuthenticationMeansSupplier.*;

public class CicGroupSampleAuthenticationMeans {
    private  Map<String, BasicAuthenticationMean> authenticationMeans;

    public CicGroupSampleAuthenticationMeans() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "clientId"));
        authenticationMeans.put(CLIENT_KEY_ID_MIGRATION, new BasicAuthenticationMean(SIGNING_KEY_ID_STRING.getType(), "d3de0198-6738-4784-92d0-a3e5e0894415"));
        authenticationMeans.put(CLIENT_NAME, new BasicAuthenticationMean(new TypedAuthenticationMeans(
                "Client Name (shown during OAuth2 flow).", StringType.getInstance(), RenderingType.ONE_LINE_STRING).getType(), "clientName"));
        authenticationMeans.put(EMAIL_ADDRESS, new BasicAuthenticationMean(CLIENT_EMAIL.getType(), "yolt@example.com"));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_MIGRATION, new BasicAuthenticationMean(KEY_ID.getType(), "d3de0198-6738-4784-92d0-a3e5e0894415"));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID, new BasicAuthenticationMean(KEY_ID.getType(), "91be0204-4c60-4a0e-a6e5-3db312358761"));
        try {
            authenticationMeans.put(SIGNING_CERTIFICATE_MIGRATION, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), loadPemFile("certificates/cic/fake-certificate.pem")));
            authenticationMeans.put(TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), loadPemFile("certificates/fake-certificate2.pem")));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public  Map<String, BasicAuthenticationMean> getBasicAuthenticationMeans() {
        return authenticationMeans;
    }

    @SneakyThrows
    public static String createPreAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String codeVerifier) {
        Region region = properties.getRegions().get(0);
        AuthorizationRedirect authRedirect = AuthorizationRedirect.createWithProofKeyCodeExchangeCodeVerifier(region.getAuthUrl(), codeVerifier);
        DataProviderState providerState = DataProviderState.preAuthorizedProviderState(region, authRedirect);
        return objectMapper.writeValueAsString(providerState);
    }

    private static String loadPemFile(final String path) throws IOException, URISyntaxException {
        URI uri = CicGroupSampleAuthenticationMeans.class
                .getClassLoader()
                .getResource(path)
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}
