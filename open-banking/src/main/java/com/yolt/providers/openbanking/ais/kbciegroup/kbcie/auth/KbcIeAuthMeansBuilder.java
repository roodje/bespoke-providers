package com.yolt.providers.openbanking.ais.kbciegroup.kbcie.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder;

import java.util.Map;

public class KbcIeAuthMeansBuilder extends KbcIeGroupAuthMeansBuilder {

    private static final String PROVIDER = "KBC_IE";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans, PROVIDER)
                .build();
    }
}
