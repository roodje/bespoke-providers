package com.yolt.providers.openbanking.ais.hsbcgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.*;

public class HsbcGroupAisTypedAuthMeansSupplier implements Supplier<Map<String, TypedAuthenticationMeans>> {

    @Override
    public Map<String, TypedAuthenticationMeans> get() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();

        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(PRIVATE_SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);

        return typedAuthenticationMeans;
    }
}