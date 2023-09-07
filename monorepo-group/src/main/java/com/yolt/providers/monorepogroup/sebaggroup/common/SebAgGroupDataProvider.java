package com.yolt.providers.monorepogroup.sebaggroup.common;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.sebaggroup.common.config.dto.internal.ProviderIdentification;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;
import java.util.Map;


@RequiredArgsConstructor
public class SebAgGroupDataProvider implements UrlDataProvider {

    private final ProviderIdentification identification;

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new NotImplementedException("TODO");
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
        return Collections.emptyMap();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
