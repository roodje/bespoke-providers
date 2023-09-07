package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;

@Deprecated // Use AccessMeansStateMapper instead C4PO-8398
public interface AccessMeansMapper<T extends AccessMeans> {
    String toJson(T accessMeans);

    T fromJson(String accessMeans) throws TokenInvalidException;
}
