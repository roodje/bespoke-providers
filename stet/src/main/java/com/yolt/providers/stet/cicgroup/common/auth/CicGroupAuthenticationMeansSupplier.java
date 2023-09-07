package com.yolt.providers.stet.cicgroup.common.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CicGroupAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    // auto onboarding AuthMeans
    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_KEY_ID_MIGRATION = "client-key-id-migration";

    // yap and hsm AuthMeans
    public static final String CLIENT_NAME = "client-name";
    public static final String EMAIL_ADDRESS = "client-email";
    public static final String SIGNING_PRIVATE_KEY_ID_MIGRATION = "client-signing-private-keyid-migration";
    public static final String SIGNING_CERTIFICATE_MIGRATION = "client-signing-certificate-migration";
    public static final String TRANSPORT_PRIVATE_KEY_ID = "client-transport-private-keyid";
    public static final String TRANSPORT_CERTIFICATE = "client-transport-certificate";

    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        autoConfiguredMeans.put(CLIENT_KEY_ID_MIGRATION, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        return autoConfiguredMeans;
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID, registrationResponse.get("client_id").textValue());
        registeredAuthMeans.put(CLIENT_KEY_ID_MIGRATION, registrationResponse.findValuesAsText("kid").stream().reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No kid from bank response")));
        return registeredAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_KEY_ID_MIGRATION, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_NAME, new TypedAuthenticationMeans(
                "Client Name (shown during OAuth2 flow).", StringType.getInstance(), RenderingType.ONE_LINE_STRING));
        typedAuthenticationMeans.put(EMAIL_ADDRESS, TypedAuthenticationMeans.CLIENT_EMAIL);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_MIGRATION, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_MIGRATION, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        return typedAuthenticationMeans;
    }

    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        return DefaultAuthenticationMeans.builder()
                .clientTransportKeyId(interpreter.getUUID(TRANSPORT_PRIVATE_KEY_ID))
                .clientTransportCertificate(interpreter.getCertificate(TRANSPORT_CERTIFICATE))
                .clientSigningKeyId(interpreter.getUUID(SIGNING_PRIVATE_KEY_ID_MIGRATION))
                .clientSigningCertificate(interpreter.getCertificate(SIGNING_CERTIFICATE_MIGRATION))
                .signingKeyIdHeader(interpreter.getNullableValue(CLIENT_KEY_ID_MIGRATION))
                .clientEmail(interpreter.getValue(EMAIL_ADDRESS))
                .clientName(interpreter.getValue(CLIENT_NAME))
                .clientId(interpreter.getNullableValue(CLIENT_ID))
                .build();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(TRANSPORT_PRIVATE_KEY_ID, TRANSPORT_CERTIFICATE));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(SIGNING_PRIVATE_KEY_ID_MIGRATION, SIGNING_CERTIFICATE_MIGRATION));
    }
}
