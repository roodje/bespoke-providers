package com.yolt.providers.monorepogroup.olbgroup.common.service.authorization;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.util.UUID;

public interface OlbGroupAuthorizationService {

    boolean isFormStep(FilledInUserSiteFormValues formValues);

    FormStep createFormStepToRetrievePsuId();

    AccessMeansOrStepDTO createRedirectStep(OlbGroupAuthenticationMeans authMeans,
                                            RestTemplateManager restTemplateManager,
                                            String baseClientRedirectUrl,
                                            String state,
                                            String psuIpAddress,
                                            FilledInUserSiteFormValues formValues);

    AccessMeansOrStepDTO createAccessMeans(String redirectUrlPostedBackFromSite,
                                           String providerState,
                                           UUID userId);


    void deleteConsent(OlbGroupAuthenticationMeans authMeans,
                       AccessMeansDTO accessMeans,
                       RestTemplateManager restTemplateManager) throws TokenInvalidException;
}
