package com.yolt.providers.axabanque.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.axabanque.common.auth.mapper.authentication.AuthenticationMeansMapper;
import com.yolt.providers.axabanque.common.auth.service.AuthenticationService;
import com.yolt.providers.axabanque.common.auth.typedauthmeans.TypedAuthenticationMeansProducer;
import com.yolt.providers.axabanque.common.fetchdata.service.FetchDataService;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.requirements.KeyRequrementsProvider;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class GroupDataProvider implements UrlDataProvider {
    private final AuthenticationService authenticationService;
    private final FetchDataService fetchDataService;
    private final KeyRequrementsProvider transportKeyRequirementsProvider;
    private final TypedAuthenticationMeansProducer typedAuthenticationMeansProducer;
    private final AuthenticationMeansMapper authenticationMeansMapper;
    private final ObjectMapper objectMapper;

    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;

    @Override
    public RedirectStep getLoginInfo(UrlGetLoginRequest request) {
        GroupAuthenticationMeans authMeans = authenticationMeansMapper.map(request.getAuthenticationMeans(), providerIdentifier);
        return authenticationService.getLoginInfo(authMeans, request.getState(), request.getBaseClientRedirectUrl(), request.getPsuIpAddress(), request.getRestTemplateManager());
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        GroupAuthenticationMeans authMeans = authenticationMeansMapper.map(request.getAuthenticationMeans(), providerIdentifier);
        return new AccessMeansOrStepDTO(authenticationService.createAccessMeans(authMeans, request.getProviderState(), request.getUserId(),
                request.getBaseClientRedirectUrl(), request.getRedirectUrlPostedBackFromSite(), request.getRestTemplateManager()));
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        GroupAccessMeans accessMeans = getAxaAccessMeans(request.getAccessMeans().getAccessMeans());
        GroupAuthenticationMeans authMeans = authenticationMeansMapper.map(request.getAuthenticationMeans(), providerIdentifier);
        return fetchDataService.fetchData(accessMeans, authMeans, request.getRestTemplateManager(),
                request.getTransactionsFetchStartTime(), request.getPsuIpAddress());
    }

    private GroupAccessMeans getAxaAccessMeans(String serializedAccessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(serializedAccessMeans, GroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize provider state");
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        GroupAccessMeans accessMeans = getAxaAccessMeans(request.getAccessMeans().getAccessMeans());
        GroupAuthenticationMeans authMeans = authenticationMeansMapper.map(request.getAuthenticationMeans(), providerIdentifier);
        AccessMeansDTO accessMeansDTO = request.getAccessMeans();
        return authenticationService.refreshAccessMeans(authMeans, accessMeans, accessMeansDTO.getUserId(), request.getRestTemplateManager());
    }


    @Override
    public void onUserSiteDelete(UrlOnUserSiteDeleteRequest urlOnUserSiteDeleteRequest) throws TokenInvalidException {
        GroupAccessMeans accessMeans = getAxaAccessMeans(urlOnUserSiteDeleteRequest.getAccessMeans().getAccessMeans());
        GroupAuthenticationMeans authMeans = authenticationMeansMapper.map(urlOnUserSiteDeleteRequest.getAuthenticationMeans(), providerIdentifier);
        authenticationService.deleteConsent(authMeans, accessMeans, urlOnUserSiteDeleteRequest.getRestTemplateManager());
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthenticationMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return transportKeyRequirementsProvider.getRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
