package com.yolt.providers.deutschebank.es;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.deutschebank.common.DeutscheBankGroupDataProvider;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeans;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducer;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClientFactory;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import com.yolt.providers.deutschebank.common.service.fetchdata.DeutscheBankGroupFetchDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DeutscheBankEsDataProviderV1 extends DeutscheBankGroupDataProvider {

    private final DeutscheBankGroupAuthorizationService authorizationService;
    private final DeutscheBankEsConsentStatusValidator consentStatusValidator;

    public DeutscheBankEsDataProviderV1(@Qualifier("DeutscheBankEsHttpClientFactory") DeutscheBankGroupHttpClientFactory httpClientFactory,
                                        @Qualifier("DeutscheBankEsAuthenticationMeansProducerV1") DeutscheBankGroupAuthenticationMeansProducer authenticationMeansProducer,
                                        @Qualifier("DeutscheBankEsAuthorizationService") DeutscheBankGroupAuthorizationService authorizationService,
                                        @Qualifier("DeutscheBankEsFetchDataService") DeutscheBankGroupFetchDataService fetchDataService,
                                        @Qualifier("DeutscheBankEsConsentStatusValidator") DeutscheBankEsConsentStatusValidator consentStatusValidator) {
        super(httpClientFactory, authorizationService, fetchDataService, authenticationMeansProducer);
        this.authorizationService = authorizationService;
        this.consentStatusValidator = consentStatusValidator;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public String getProviderIdentifier() {
        return "DEUTSCHE_BANK_ES";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Deutsche Bank Spain";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest request) {
        DeutscheBankGroupAuthenticationMeans authMeans = createAuthMeans(request.getAuthenticationMeans());
        DeutscheBankGroupHttpClient httpClient = createHttpClient(authMeans, request.getRestTemplateManager());
        ConsentCreationResponse consentCreationResponse = authorizationService.createConsent(httpClient, request);
        String consentId = consentCreationResponse.getConsentId();
        DeutscheBankGroupProviderState deutscheBankGroupProviderState = new DeutscheBankGroupProviderState(consentId);
        consentStatusValidator.validate(httpClient, consentId, request.getPsuIpAddress());
        return authorizationService.createAccessMeans(deutscheBankGroupProviderState, request.getUserId());
    }
}
