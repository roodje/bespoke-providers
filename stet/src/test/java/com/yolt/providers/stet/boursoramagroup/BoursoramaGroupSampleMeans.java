package com.yolt.providers.stet.boursoramagroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
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
import java.util.UUID;

import static com.yolt.providers.stet.boursoramagroup.boursorama.auth.BoursoramaAuthenticationMeansSupplier.*;

@UtilityClass
class BoursoramaGroupSampleMeans {

    private static final String CERTIFICATE_PATH = "certificates/boursorama/fake-certificate.pem";
    private static final Map<String, BasicAuthenticationMean> AUTH_MEANS = new HashMap<>();

    static {
        Security.addProvider(new BouncyCastleProvider());
        AUTH_MEANS.put(CLIENT_TRANSPORT_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        AUTH_MEANS.put(CLIENT_TRANSPORT_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM.getType(), readCertificate()));
        AUTH_MEANS.put(CLIENT_SIGNING_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));
        AUTH_MEANS.put(CLIENT_SIGNING_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), readCertificate()));
        AUTH_MEANS.put(CLIENT_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(), "PSDFR-ACPR-XXXXX"));
        AUTH_MEANS.put(CERTIFICATE_AGREEMENT_NUMBER, new BasicAuthenticationMean(TypedAuthenticationMeans.CERTIFICATE_AGREEMENT_NUMBER_STRING.getType(), "TEST-AGREEMENT-NUMBER"));
    }

    Map<String, BasicAuthenticationMean> getAuthMeans() {
        return AUTH_MEANS;
    }

    @SneakyThrows
    private static String readCertificate() {
        URL certificateUrl = BoursoramaGroupTestConfig.class.getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
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

    @SneakyThrows
    String createPaymentJsonProviderState(ObjectMapper objectMapper, String paymentId) {
        PaymentProviderState providerState = PaymentProviderState.initiatedProviderState(paymentId);
        return objectMapper.writeValueAsString(providerState);
    }
}
