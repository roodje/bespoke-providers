package com.yolt.providers.deutschebank.de;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.deutschebank.common.DeutscheBankGroupDataProvider;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducer;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClientFactory;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import com.yolt.providers.deutschebank.common.service.fetchdata.DeutscheBankGroupFetchDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DeutscheBankDataProviderV1 extends DeutscheBankGroupDataProvider {


    public DeutscheBankDataProviderV1(@Qualifier("DeutscheBankHttpClientFactory") DeutscheBankGroupHttpClientFactory httpClientFactory,
                                      @Qualifier("DeutscheBankAuthenticationMeansProducerV1") DeutscheBankGroupAuthenticationMeansProducer authenticationMeansProducer,
                                      @Qualifier("DeutscheBankAuthorizationService") DeutscheBankGroupAuthorizationService authorizationService,
                                      @Qualifier("DeutscheBankFetchDataService") DeutscheBankGroupFetchDataService fetchDataService) {
        super(httpClientFactory, authorizationService, fetchDataService, authenticationMeansProducer);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public String getProviderIdentifier() {
        return "DEUTSCHE_BANK";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Deutsche Bank";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }
}
