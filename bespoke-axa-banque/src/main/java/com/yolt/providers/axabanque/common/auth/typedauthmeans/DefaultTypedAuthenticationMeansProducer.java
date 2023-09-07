package com.yolt.providers.axabanque.common.auth.typedauthmeans;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;

public class DefaultTypedAuthenticationMeansProducer implements TypedAuthenticationMeansProducer {
    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_ID, CLIENT_ID_STRING);
        return typedAuthenticationMeans;
    }
}
