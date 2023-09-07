package com.yolt.providers.stet.societegeneralegroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class SocieteGeneraleAuthenticationMeansSupplier implements AuthenticationMeansSupplier {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_TRANSPORT_KEY_ID_ROTATION = "client-transport-private-key-id-rotation";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_ROTATION = "client-transport-certificate-rotation";
    public static final String CLIENT_SIGNING_KEY_ID_ROTATION = "client-signing-private-key-id-rotation";
    public static final String CLIENT_SIGNING_CERTIFICATE_ROTATION = "client-signing-certificate-rotation";

    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SIGNING_KEY_ID_ROTATION, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_SIGNING_CERTIFICATE_ROTATION, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        return typedAuthenticationMeansMap;
    }

    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        DefaultAuthenticationMeans authenticationMeans = DefaultAuthenticationMeans.builder()
                .clientId(interpreter.getValue(CLIENT_ID_NAME))
                .clientSecret(interpreter.getValue(CLIENT_SECRET_NAME))
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_KEY_ID_ROTATION))
                .signingKeyIdHeader(interpreter.getValue(CLIENT_SIGNING_KEY_ID_ROTATION))
                .clientSigningCertificate(interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE_ROTATION))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID_ROTATION))
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_ROTATION))
                .build();
        return authenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_TRANSPORT_KEY_ID_ROTATION, CLIENT_TRANSPORT_CERTIFICATE_ROTATION));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_SIGNING_KEY_ID_ROTATION, CLIENT_SIGNING_CERTIFICATE_ROTATION));
    }
}
