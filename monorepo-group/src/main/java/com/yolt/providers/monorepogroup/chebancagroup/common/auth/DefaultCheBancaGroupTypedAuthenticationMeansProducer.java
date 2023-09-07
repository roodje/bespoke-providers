package com.yolt.providers.monorepogroup.chebancagroup.common.auth;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans.*;

public class DefaultCheBancaGroupTypedAuthenticationMeansProducer implements CheBancaGroupTypedAuthenticationMeansProducer {

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(TRANSPORT_CERTIFICATE_ID_NAME, KEY_ID);
        typedAuthenticationMeansMap.put(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_ID_NAME, KEY_ID);
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_NAME, CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(CLIENT_ID_NAME, CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING);
        typedAuthenticationMeansMap.put(CLIENT_APP_ID, ALIAS_STRING);
        return typedAuthenticationMeansMap;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoconfigureTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> autoonboardingTypedAuthenticationMeansMap = new HashMap<>();
        autoonboardingTypedAuthenticationMeansMap.put(CLIENT_ID_NAME, CLIENT_ID_STRING);
        autoonboardingTypedAuthenticationMeansMap.put(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING);
        return autoonboardingTypedAuthenticationMeansMap;
    }


    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(TRANSPORT_CERTIFICATE_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(SIGNING_CERTIFICATE_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }
}
