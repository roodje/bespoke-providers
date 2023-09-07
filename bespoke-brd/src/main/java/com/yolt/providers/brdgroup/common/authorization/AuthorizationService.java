package com.yolt.providers.brdgroup.common.authorization;

import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;

import java.util.UUID;

public interface AuthorizationService {

    FormStep generateLoginIdForm();

    AccessMeansOrStepDTO createAccessMeans(BrdGroupHttpClient httpClient, String psuIpAddress, String loginId, UUID userId);

    void deleteConsent(BrdGroupHttpClient httpClient, String accessMeans);
}
