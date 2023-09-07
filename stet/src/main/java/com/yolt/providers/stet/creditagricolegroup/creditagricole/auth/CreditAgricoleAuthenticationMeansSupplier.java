package com.yolt.providers.stet.creditagricolegroup.creditagricole.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;

@RequiredArgsConstructor
public class CreditAgricoleAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    private static final NoWhiteCharacterStringType NO_WHITE_CHARACTER_STRING = NoWhiteCharacterStringType.getInstance();

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SIGNING_CERTIFICATE_NAME = "signing-certificate-new";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "transport-certificate-new";
    public static final String CLIENT_SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-new";
    public static final String CLIENT_TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-new";
    public static final String CLIENT_EMAIL_NAME = "client-contact-email";
    public static final String CLIENT_NAME = "client-name";

    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_UUID);
        return typedAuthMeans;
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID_NAME, registrationResponse.get("client_id").textValue());
        return registeredAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> authMeans = new HashMap<>();
        authMeans.put(CLIENT_TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        authMeans.put(CLIENT_SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        authMeans.put(CLIENT_EMAIL_NAME, new TypedAuthenticationMeans("Client contact email address", NO_WHITE_CHARACTER_STRING, ONE_LINE_STRING));
        authMeans.put(CLIENT_NAME, new TypedAuthenticationMeans("Client name", NO_WHITE_CHARACTER_STRING, ONE_LINE_STRING));
        authMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        return authMeans;
    }

    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        return DefaultAuthenticationMeans.builder()
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_PRIVATE_KEY_ID_NAME))
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_NAME))
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_PRIVATE_KEY_ID_NAME))
                .clientSigningCertificate(interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE_NAME))
                .signingKeyIdHeader(interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE_NAME).getSerialNumber().toString(16))
                .clientEmail(interpreter.getValue(CLIENT_EMAIL_NAME))
                .clientName(interpreter.getValue(CLIENT_NAME))
                .clientId(interpreter.getNullableValue(CLIENT_ID_NAME))
                .build();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_TRANSPORT_PRIVATE_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_SIGNING_PRIVATE_KEY_ID_NAME, CLIENT_SIGNING_CERTIFICATE_NAME));
    }
}
