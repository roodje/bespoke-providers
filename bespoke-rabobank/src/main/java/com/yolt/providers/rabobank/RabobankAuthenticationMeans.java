package com.yolt.providers.rabobank;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Builder;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class RabobankAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-certificate";
    public static final String CLIENT_SIGNING_CERTIFICATE = "client-signing-certificate";
    public static final String CLIENT_SIGNING_KEY_ID = "client-signing-private-keyid";
    public static final String CLIENT_TRANSPORT_KEY_ID = "client-transport-private-keyid";

    private static final String PROVIDER = "Rabobank";

    private final String clientId;
    private final String clientSecret;
    private final X509Certificate clientCertificate;
    private final X509Certificate clientSigningCertificate;
    private final UUID signingKid;
    private final UUID transportKid;

    static RabobankAuthenticationMeans fromAISAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return createCommonAuthenticationMeans(typedAuthenticationMeans).clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET_NAME).getValue()).build();
    }

    public static RabobankAuthenticationMeans fromPISAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return createCommonAuthenticationMeans(typedAuthenticationMeans).build();
    }

    public static RabobankAuthenticationMeans.RabobankAuthenticationMeansBuilder createCommonAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        String signingKid = typedAuthenticationMeans.get(CLIENT_SIGNING_KEY_ID).getValue();
        String transportKid = typedAuthenticationMeans.get(CLIENT_TRANSPORT_KEY_ID).getValue();
        X509Certificate clientSigningCertificate = createCertificate(typedAuthenticationMeans.get(CLIENT_SIGNING_CERTIFICATE).getValue());
        X509Certificate clientTransportCertificate = createCertificate(typedAuthenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE).getValue());

        return RabobankAuthenticationMeans.builder()
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .clientSigningCertificate(clientSigningCertificate)
                .signingKid(UUID.fromString(signingKid))
                .clientCertificate(clientTransportCertificate)
                .transportKid(UUID.fromString(transportKid));
    }

    private static X509Certificate createCertificate(final String certificateString) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(PROVIDER, CLIENT_TRANSPORT_CERTIFICATE, "Cannot process certificate for thumbprint");
        }
    }

    public static Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeansForAISAndPIS() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_SIGNING_CERTIFICATE, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);

        return typedAuthenticationMeansMap;
    }
}