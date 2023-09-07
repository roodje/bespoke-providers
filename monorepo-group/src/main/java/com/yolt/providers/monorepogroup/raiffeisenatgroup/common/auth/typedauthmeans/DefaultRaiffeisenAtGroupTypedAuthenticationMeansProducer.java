package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.auth.typedauthmeans;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans.*;

public class DefaultRaiffeisenAtGroupTypedAuthenticationMeansProducer implements RaiffeisenAtGroupTypedAuthenticationMeansProducer {
    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(TRANSPORT_CERTIFICATE_ID_NAME, KEY_ID);
        typedAuthenticationMeansMap.put(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(CLIENT_ID_NAME, CLIENT_ID_STRING);
        return typedAuthenticationMeansMap;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoconfigureTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> autoonboardingTypedAuthenticationMeansMap = new HashMap<>();
        autoonboardingTypedAuthenticationMeansMap.put(CLIENT_ID_NAME, CLIENT_ID_STRING);
        return autoonboardingTypedAuthenticationMeansMap;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(TRANSPORT_CERTIFICATE_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }
}
