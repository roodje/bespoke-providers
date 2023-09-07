package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;

public interface AccessMeansStateMapper<T extends AccessMeansState> {
    String toJson(T accessMeansState);

    T fromJson(String accessMeansState) throws TokenInvalidException;
}
