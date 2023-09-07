package com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface AtruviaGroupAuthenticationMeansFactory {

    String CLIENT_SIGNING_CERTIFICATE_NAME = "client-signing-certificate";
    String CLIENT_SIGNING_KEY_ID_NAME = "client-signing-private-keyid";

    Map<String, TypedAuthenticationMeans> TYPED_AUTHENTICATION_MEANS = Map.of(
            CLIENT_SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM,
            CLIENT_SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING
    );

    default Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return TYPED_AUTHENTICATION_MEANS;
    }

    default Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(CLIENT_SIGNING_KEY_ID_NAME, CLIENT_SIGNING_CERTIFICATE_NAME);
    }

    AtruviaGroupAuthenticationMeans toAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeanMap);
}
