package com.yolt.providers.axabanque.common.auth.service;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.UUID;


public interface AuthenticationService {
    RedirectStep getLoginInfo(GroupAuthenticationMeans authMeans, String state, String baseClientRedirectUrl, String psuIpAddress, RestTemplateManager restTemplateManager);

    AccessMeansDTO createAccessMeans(GroupAuthenticationMeans authMeans, String serializedProviderState, UUID userId, String baseRedirectUri, String redirectUriPostedBackFromSite, RestTemplateManager restTemplateManager);

    AccessMeansDTO refreshAccessMeans(GroupAuthenticationMeans authMeans, GroupAccessMeans accessMeans, UUID userId, RestTemplateManager restTemplateManager) throws TokenInvalidException;

    void deleteConsent(GroupAuthenticationMeans authMeans, GroupAccessMeans accessMeans, RestTemplateManager restTemplateManager) throws TokenInvalidException;
}
