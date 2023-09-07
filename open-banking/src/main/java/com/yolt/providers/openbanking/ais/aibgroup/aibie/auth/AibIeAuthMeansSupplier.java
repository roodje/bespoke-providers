package com.yolt.providers.openbanking.ais.aibgroup.aibie.auth;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3.*;


public class AibIeAuthMeansSupplier implements Supplier<Map<String, TypedAuthenticationMeans>> {

    @Override
    public Map<String, TypedAuthenticationMeans> get() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }
}
