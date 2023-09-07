package com.yolt.providers.nutmeggroup.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import lombok.Data;

import java.util.Map;

@Data
public class AuthenticationMeansV2 {
    public static final String CLIENT_ID = "client-id";

    private final String clientId;

    public static AuthenticationMeansV2 getAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                               final String provider) {

        return new AuthenticationMeansV2(
                getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider)
        );
    }

    private static String getAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                     final String key,
                                                     final String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }

}