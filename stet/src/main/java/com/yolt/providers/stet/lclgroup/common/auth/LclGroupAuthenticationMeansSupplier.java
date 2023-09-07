package com.yolt.providers.stet.lclgroup.common.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration.*;

@RequiredArgsConstructor
public class LclGroupAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        return autoConfiguredMeans;
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID_NAME, registrationResponse.get("client_id").textValue());
        return registeredAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedClientConfigurationMap = new HashMap<>();
        typedClientConfigurationMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedClientConfigurationMap.put(CLIENT_SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedClientConfigurationMap.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedClientConfigurationMap.put(CLIENT_SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedClientConfigurationMap.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedClientConfigurationMap.put(CLIENT_NAME_NAME, new TypedAuthenticationMeans("Client name",
                StringType.getInstance(), ONE_LINE_STRING));
        typedClientConfigurationMap.put(CLIENT_CONTACT_EMAIL_NAME, new TypedAuthenticationMeans("Client contact email address",
                NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING));
        typedClientConfigurationMap.put(PROVIDER_LEGAL_ID_NAME, new TypedAuthenticationMeans("Provider legal id",
                NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING));
        return typedClientConfigurationMap;
    }

    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        return LclGroupClientConfiguration.fromAuthenticationMeans(basicAuthMeans, providerIdentifier);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_SIGNING_KEY_ID_NAME, CLIENT_SIGNING_CERTIFICATE_NAME));
    }
}
