package com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.PASSWORD;

@RequiredArgsConstructor
public class LaBanquePostaleAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    private static final NoWhiteCharacterStringType NO_WHITE_CHARACTER_STRING = NoWhiteCharacterStringType.getInstance();
    private static final StringType STRING = StringType.getInstance();

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-key-id";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_CHAIN_NAME = "client-transport-certificate-chain";
    public static final String CLIENT_EMAIL_ADDRESS_NAME = "client-contact-email-address";
    public static final String PORTAL_USERNAME_NAME = "client-api-portal-username";
    @SuppressWarnings("squid:S2068") // Not a real password. It's just a key.
    public static final String PORTAL_PASSWORD_NAME = "client-api-portal-password";
    public static final String CLIENT_NAME_NAME = "client-name";


    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return typedAuthMeans;
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID_NAME, registrationResponse.get("client_id").textValue());
        registeredAuthMeans.put(CLIENT_SECRET_NAME, registrationResponse.get("client_secret").textValue());
        return registeredAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(CLIENT_TRANSPORT_CERTIFICATE_CHAIN_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATES_CHAIN_PEM);
        typedAuthMeans.put(CLIENT_EMAIL_ADDRESS_NAME, new TypedAuthenticationMeans("Contact email address", NO_WHITE_CHARACTER_STRING, ONE_LINE_STRING));
        typedAuthMeans.put(PORTAL_USERNAME_NAME, new TypedAuthenticationMeans("Username of API portal at the bank", NO_WHITE_CHARACTER_STRING, ONE_LINE_STRING));
        typedAuthMeans.put(PORTAL_PASSWORD_NAME, new TypedAuthenticationMeans("Password of API portal at the bank", NO_WHITE_CHARACTER_STRING, PASSWORD));
        typedAuthMeans.put(CLIENT_NAME_NAME, new TypedAuthenticationMeans("Client (application) name of the registration ", STRING, ONE_LINE_STRING));
        return typedAuthMeans;
    }

    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        return LaBanquePostaleAuthenticationMeans.extendedBuilder()
                .clientId(interpreter.getNullableValue(CLIENT_ID_NAME))
                .clientSecret(interpreter.getNullableValue(CLIENT_SECRET_NAME))
                .clientName(interpreter.getValue(CLIENT_NAME_NAME))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID_NAME))
                .clientTransportCertificateChain(interpreter.getCertificateChain(CLIENT_TRANSPORT_CERTIFICATE_CHAIN_NAME))
                .clientEmail(interpreter.getValue(CLIENT_EMAIL_ADDRESS_NAME))
                .portalUsername(interpreter.getValue(PORTAL_USERNAME_NAME))
                .portalPassword(interpreter.getValue(PORTAL_PASSWORD_NAME))
                .build();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_CHAIN_NAME));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.empty();
    }
}
