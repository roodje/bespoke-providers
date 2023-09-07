package com.yolt.providers.redsys.common.model;

import com.yolt.providers.common.exception.TokenInvalidException;

public interface AccessMeansSerializer {
    String serialize(RedsysAccessMeans redsysAccessMeans);

    RedsysAccessMeans deserialize(String accessMean) throws TokenInvalidException;
}
