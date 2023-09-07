package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;

import java.util.List;

public interface AccessMeansStateProvider<U extends AccessMeansState> {
    U apply(AccessMeans accessToken, List<String> permissions);
}
