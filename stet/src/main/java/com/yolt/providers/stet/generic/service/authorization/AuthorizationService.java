package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import nl.ing.lovebird.providershared.AccessMeansDTO;

public interface AuthorizationService {

    Step getStep(StepRequest stepRequest);

    AccessMeansOrStepDTO createAccessMeansOrGetStep(HttpClient httpClient, AccessMeansOrStepRequest accessMeansOrStepRequest) throws TokenInvalidException;

    AccessMeansDTO refreshAccessMeans(HttpClient httpClient, AccessMeansRequest accessMeansRequest) throws TokenInvalidException;
}
