package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.unicredit.common.util.HsmUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.*;


public class UniCreditAuthenticationMeansProducerV1 implements UniCreditAuthenticationMeansProducer {

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        return Collections.singletonMap(REGISTRATION_STATUS, TypedAuthenticationMeans.TPP_ID);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> authMeans = new HashMap<>();
        authMeans.put(EIDAS_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        authMeans.put(EIDAS_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(CLIENT_EMAIL, TypedAuthenticationMeans.CLIENT_EMAIL);
        //Auth mean used for store status of registration - not visible in YAP for user
        authMeans.put(REGISTRATION_STATUS, TypedAuthenticationMeans.TPP_ID);
        return authMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(EIDAS_KEY_ID, EIDAS_CERTIFICATE);
    }
}
