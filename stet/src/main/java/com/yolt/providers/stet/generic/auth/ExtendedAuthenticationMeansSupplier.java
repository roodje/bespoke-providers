package com.yolt.providers.stet.generic.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.Map;

public interface ExtendedAuthenticationMeansSupplier extends AuthenticationMeansSupplier {

    Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans();

    Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse);
}
