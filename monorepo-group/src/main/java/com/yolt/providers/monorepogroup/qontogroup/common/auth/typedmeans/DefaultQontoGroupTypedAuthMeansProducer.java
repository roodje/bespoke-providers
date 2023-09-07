package com.yolt.providers.monorepogroup.qontogroup.common.auth.typedmeans;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans.*;


public class DefaultQontoGroupTypedAuthMeansProducer implements QontoGroupTypedAuthMeansProducer {
    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);

        return typedAuthenticationMeansMap;
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(SIGNING_CERTIFICATE_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }
}
