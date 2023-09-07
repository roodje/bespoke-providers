package com.yolt.providers.stet.generic.service.authorization.refresh;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import nl.ing.lovebird.providershared.AccessMeansDTO;

public interface RefreshTokenStrategy {

    AccessMeansDTO refreshAccessMeans(HttpClient httpClient, AccessMeansRequest request) throws TokenInvalidException;
}
