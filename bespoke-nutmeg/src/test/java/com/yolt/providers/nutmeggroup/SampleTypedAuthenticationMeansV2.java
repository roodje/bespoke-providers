package com.yolt.providers.nutmeggroup;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.nutmeggroup.common.AuthenticationMeansV2.CLIENT_ID;

@Getter
public class SampleTypedAuthenticationMeansV2 {

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    public SampleTypedAuthenticationMeansV2() {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "clientId"));
    }
}
