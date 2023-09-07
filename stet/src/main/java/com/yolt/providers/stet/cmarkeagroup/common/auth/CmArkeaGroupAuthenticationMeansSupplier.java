package com.yolt.providers.stet.cmarkeagroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.signer.signature.Fingerprint;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CmArkeaGroupAuthenticationMeansSupplier implements AuthenticationMeansSupplier {

    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";
    public static final String CLIENT_TRANSPORT_KEY_ID = "client-transport-private-keyid";
    public static final String CLIENT_SIGNING_KEY_ID = "client-signing-private-keyid";
    public static final String CLIENT_SIGNING_CERTIFICATE = "client-signing-certificate";
    public static final String CLIENT_ID = "client-id";
    private static final String PEM_FORMAT_EXTENSION = ".pem";

    private final KeyRequirementsProducer keyRequirementsProducer;
    private final DefaultProperties properties;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {

        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_SIGNING_CERTIFICATE, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        return typedAuthenticationMeans;
    }

    @Override
    @SneakyThrows(CertificateEncodingException.class)
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        X509Certificate signingCertificate = interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE);
        return DefaultAuthenticationMeans.builder()
                .clientId(interpreter.getValue(CLIENT_ID))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID))
                .clientSigningCertificate(signingCertificate)
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_KEY_ID))
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE))
                .signingKeyIdHeader(getCertificateUrl(signingCertificate, properties.getS3baseUrl()))
                .build();
    }

    private String getCertificateUrl(X509Certificate certificate, String s3BaseUrl) throws CertificateEncodingException {

        final String fingerprint = new Fingerprint(certificate.getEncoded()).toString();
        return s3BaseUrl + "/" + fingerprint + PEM_FORMAT_EXTENSION;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_TRANSPORT_KEY_ID, CLIENT_TRANSPORT_CERTIFICATE));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_SIGNING_KEY_ID, CLIENT_SIGNING_CERTIFICATE));
    }
}
