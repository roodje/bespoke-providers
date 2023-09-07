package com.yolt.providers.stet.boursoramagroup.boursorama.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class BoursoramaAuthenticationMeansSupplier implements AuthenticationMeansSupplier {

    public static final String CLIENT_TRANSPORT_KEY_ID = "client-transport-private-keyid";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";
    public static final String CLIENT_SIGNING_KEY_ID = "client-signing-private-keyid";
    public static final String CLIENT_SIGNING_CERTIFICATE = "client-signing-certificate";
    public static final String CLIENT_ID = "client-id";
    public static final String CERTIFICATE_AGREEMENT_NUMBER = "certificate-agreement-number";

    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_SIGNING_CERTIFICATE, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CERTIFICATE_AGREEMENT_NUMBER, TypedAuthenticationMeans.CERTIFICATE_AGREEMENT_NUMBER_STRING);
        return typedAuthenticationMeans;
    }

    @SneakyThrows(CertificateEncodingException.class)
    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        return BoursoramaAuthenticationMeans.extendedBuilder()
                .clientId(interpreter.getValue(CLIENT_ID))
                .clientSigningCertificate(interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE))
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_KEY_ID))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID))
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE))
                .certificateAgreementNumber(interpreter.getValue(CERTIFICATE_AGREEMENT_NUMBER))
                .signingKeyIdHeader(new String(Base64.getEncoder().encode(interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE).getEncoded())))
                .build();
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
