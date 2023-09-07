package com.yolt.providers.axabanque.common.auth.typedauthmeans;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.Map;

public interface TypedAuthenticationMeansProducer {
    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();
}
