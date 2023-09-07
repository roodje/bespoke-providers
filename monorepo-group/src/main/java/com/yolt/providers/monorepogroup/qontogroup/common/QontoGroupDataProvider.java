package com.yolt.providers.monorepogroup.qontogroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.qontogroup.common.auth.typedmeans.QontoGroupTypedAuthMeansProducer;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.ProviderIdentification;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClientProducer;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.ProviderStateProcessingException;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.QontoGroupAuthenticationMeansMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.QontoGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.service.QontoFetchDataService;
import com.yolt.providers.monorepogroup.qontogroup.common.service.QontoGroupAuthenticationService;
import com.yolt.providers.monorepogroup.qontogroup.common.service.QontoGroupMappingService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class QontoGroupDataProvider implements UrlDataProvider {

    private final Clock clock;
    private final ProviderIdentification identification;
    private final QontoGroupTypedAuthMeansProducer typedAuthMeansProducer;
    private final QontoGroupAuthenticationMeansMapper authenticationMeansMapper;
    private final QontoGroupAuthenticationService authenticationService;
    private final QontoGroupHttpClientProducer httpClientProducer;
    private final QontoGroupProviderStateMapper providerStateMapper;
    private final QontoFetchDataService fetchDataService;
    private final QontoGroupMappingService mappingService;


    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        var authenticationMeans = authenticationMeansMapper.map(urlFetchData.getAuthenticationMeans(), identification.getProviderIdentifier());
        var httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlFetchData.getRestTemplateManager());
        try {
            var providerState = providerStateMapper.deserialize(urlFetchData.getAccessMeans().getAccessMeans());
            var fetchDataResult = fetchDataService.fetchAccount(authenticationMeans,
                    httpClient,
                    providerState,
                    urlFetchData.getTransactionsFetchStartTime(),
                    urlFetchData.getPsuIpAddress(),
                    urlFetchData.getSigner());
            List<ProviderAccountDTO> mappedAccounts = mappingService.mapToListOfProviderAccountDto(fetchDataResult);
            return new DataProviderResponse(mappedAccounts);
        } catch (ProviderStateProcessingException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        var authenticationMeans = authenticationMeansMapper.map(urlGetLogin.getAuthenticationMeans(), identification.getProviderIdentifier());
        var redirectUrl = authenticationService.getLoginUrl(authenticationMeans,
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState());
        return new RedirectStep(redirectUrl);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        var authenticationMeans = authenticationMeansMapper.map(urlCreateAccessMeans.getAuthenticationMeans(), identification.getProviderIdentifier());
        var httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlCreateAccessMeans.getRestTemplateManager());
        try {
            var providerState = authenticationService.createAccessMeans(authenticationMeans,
                    httpClient,
                    urlCreateAccessMeans.getBaseClientRedirectUrl(),
                    urlCreateAccessMeans.getRedirectUrlPostedBackFromSite());
            var serializedProviderState = providerStateMapper.serialize(providerState);
            return new AccessMeansOrStepDTO(
                    new AccessMeansDTO(urlCreateAccessMeans.getUserId(),
                            serializedProviderState,
                            new Date(clock.millis()),
                            new Date(providerState.getExpirationTimeInMillis())
                    ));
        } catch (ProviderStateProcessingException | TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Failed to create access means", e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        var authenticationMeans = authenticationMeansMapper.map(urlRefreshAccessMeans.getAuthenticationMeans(), identification.getProviderIdentifier());
        var httpClient = httpClientProducer.createHttpClient(authenticationMeans, urlRefreshAccessMeans.getRestTemplateManager());
        try {
            var providerState = providerStateMapper.deserialize(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
            var refreshedProviderState = authenticationService.refreshAccessMeans(authenticationMeans, providerState, httpClient);
            var serializedRefreshedProviderState = providerStateMapper.serialize(refreshedProviderState);
            return new AccessMeansDTO(urlRefreshAccessMeans.getAccessMeans().getUserId(),
                    serializedRefreshedProviderState,
                    new Date(clock.millis()),
                    new Date(refreshedProviderState.getExpirationTimeInMillis())
            );
        } catch (ProviderStateProcessingException e) {
            throw new TokenInvalidException("Error occurred during serialization/deserialization of provider state", e);
        }
    }

    @Override
    public String getProviderIdentifier() {
        return identification.getProviderIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return identification.getProviderDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return identification.getVersion();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return typedAuthMeansProducer.getTypedAuthenticationMeans();
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return typedAuthMeansProducer.getSigningKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
