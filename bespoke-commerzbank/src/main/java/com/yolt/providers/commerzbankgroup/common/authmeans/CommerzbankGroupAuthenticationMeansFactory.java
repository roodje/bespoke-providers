package com.yolt.providers.commerzbankgroup.common.authmeans;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface CommerzbankGroupAuthenticationMeansFactory {

    String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";

    Map<String, TypedAuthenticationMeans> TYPED_AUTHENTICATION_MEANS = Map.of(
            CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM,
            CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING
    );

    default Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return TYPED_AUTHENTICATION_MEANS;
    }

    default Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    CommerzbankGroupAuthenticationMeans toAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeanMap);
}
