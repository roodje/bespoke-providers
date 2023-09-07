package com.yolt.providers.openbanking.ais.permanenttsbgroup.permanenttsb.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.auth.PermanentTsbGroupAuthMeansBuilder;

import java.util.Map;

public class PermanentTsbAuthMeansBuilder extends PermanentTsbGroupAuthMeansBuilder {

    private static final String PROVIDER = "PERMANENT_TSB";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans, PROVIDER)
                .build();
    }
}
