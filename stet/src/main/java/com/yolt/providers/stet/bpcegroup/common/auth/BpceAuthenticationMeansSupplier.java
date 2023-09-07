package com.yolt.providers.stet.bpcegroup.common.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration.CLIENT_SIGNING_CERTIFICATE_NAME;

@RequiredArgsConstructor
public class BpceAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SIGNING_KEY_ID = "client-signing-private-keyid";
    public static final String CLIENT_TRANSPORT_KEY_ID = "client-transport-private-keyid";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";

    public static final String CLIENT_SIGNING_CERTIFICATE = "client-signing-certificate";
    public static final String CLIENT_EMAIL = "client-email";
    public static final String CLIENT_NAME = "client-name";
    public static final String CLIENT_PHONE_NUMBER = "client-phone-number";

    public static final TypedAuthenticationMeans CLIENT_NAME_TYPE = new TypedAuthenticationMeans("Client (application) name of the registration ", StringType.getInstance(), ONE_LINE_STRING);
    public static final TypedAuthenticationMeans CLIENT_PHONE_NUMBER_TYPE = new TypedAuthenticationMeans("Client contact phone number", NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING);


    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);

        // The below were added when introduced autoonboarding under C4PO-10110. These need to be nullable as per existing registrations. The old migrations has to be migrated, before this can be changed to nonnullable
        typedAuthenticationMeansMap.put(CLIENT_SIGNING_CERTIFICATE, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(CLIENT_EMAIL, TypedAuthenticationMeans.CLIENT_EMAIL);
        typedAuthenticationMeansMap.put(CLIENT_NAME, CLIENT_NAME_TYPE);
        typedAuthenticationMeansMap.put(CLIENT_PHONE_NUMBER, CLIENT_PHONE_NUMBER_TYPE);
        return typedAuthenticationMeansMap;
    }

    @Override
    public BpceGroupAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        X509Certificate certificate = null;
        try {
            certificate = interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE_NAME);
        } catch (Exception ignored) { }
        return BpceGroupAuthenticationMeans.extendedBuilder()
                .clientId(interpreter.getNullableValue(CLIENT_ID))
                // unlike other banks bpce requests client id here as keyId header
                .signingKeyIdHeader(interpreter.getNullableValue(CLIENT_ID))
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_KEY_ID))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID))
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE))
                .clientEmail(interpreter.getNullableValue(CLIENT_EMAIL))
                .clientName(interpreter.getNullableValue(CLIENT_NAME))
                .clientSigningCertificate(certificate)
                .contactPhone(interpreter.getNullableValue(CLIENT_PHONE_NUMBER))
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

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        return typedAuthenticationMeansMap;
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID, registrationResponse.get("client_id").textValue());
        return registeredAuthMeans;
    }
}
