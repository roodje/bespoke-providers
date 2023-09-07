package com.yolt.providers.stet.bnpparibasfortisgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.stet.generic.config.DefaultProperties;
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

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth.BnpParibasFortisAuthenticationMeansSupplier.*;

class BnpParibasFortisGroupSampleMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";

    Map<String, BasicAuthenticationMean> getConfiguredAuthMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = getPreconfiguredAuthMeans();
        authenticationMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "client-id"));
        authenticationMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "client-secret"));
        return authenticationMeans;
    }

    Map<String, BasicAuthenticationMean> getPreconfiguredAuthMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();

        authenticationMeans.put(CLIENT_NAME, new BasicAuthenticationMean(CLIENT_NAME_TYPE.getType(), "Yolt Application"));
        authenticationMeans.put(CLIENT_DESCRIPTION, new BasicAuthenticationMean(CLIENT_DESCRIPTION_TYPE.getType(), "Third Party Provider"));
        authenticationMeans.put(CLIENT_WEBSITE_URI, new BasicAuthenticationMean(CLIENT_WEBSITE_URI_TYPE.getType(), "https://www.yolt.com/"));
        authenticationMeans.put(CONTACT_FIRST_NAME, new BasicAuthenticationMean(CONTACT_FIRST_NAME_TYPE.getType(), "John"));
        authenticationMeans.put(CONTACT_LAST_NAME, new BasicAuthenticationMean(CONTACT_LAST_NAME_TYPE.getType(), "Smith"));
        authenticationMeans.put(CONTACT_EMAIL, new BasicAuthenticationMean(CONTACT_EMAIL_TYPE.getType(), "example@yolt.com"));
        authenticationMeans.put(CONTACT_PHONE, new BasicAuthenticationMean(CONTACT_PHONE_TYPE.getType(), "0001123321123"));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "5391cac7-b840-4628-8036-d4998dfb8959"));
        return authenticationMeans;
    }

    String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }

    @SneakyThrows
    public static String createAuthorizedJsonProviderState(ObjectMapper objectMapper, DefaultProperties properties, String accessToken) {
        Region region = properties.getRegions().get(0);
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, accessToken);
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    static DataProviderState deserializeProviderState(ObjectMapper objectMapper, String accessMeans) {
        return objectMapper.readValue(accessMeans, DataProviderState.class);
    }
}
