package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;

import java.util.List;

public class DefaultAccessMeansStateProvider implements AccessMeansStateProvider<AccessMeansState> {
    @Override
    public AccessMeansState apply(AccessMeans accessToken, List<String> permissions){
        return new AccessMeansState(accessToken, permissions);
    }
}
