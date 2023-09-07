package com.yolt.providers.dkbgroup.common.dynamicflow;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.dkbgroup.common.http.DKBGroupHttpClient;

public interface DynamicFlowHandler {
    AccessMeansOrStepDTO handle(UrlCreateAccessMeansRequest urlCreateAccessMeans, DKBGroupHttpClient httpClient) throws TokenInvalidException;
}
