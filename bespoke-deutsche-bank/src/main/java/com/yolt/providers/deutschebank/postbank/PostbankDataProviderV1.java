package com.yolt.providers.deutschebank.postbank;

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
public class PostbankDataProviderV1 extends DeutscheBankGroupDataProvider {

    public PostbankDataProviderV1(@Qualifier("PostbankHttpClientFactory") DeutscheBankGroupHttpClientFactory httpClientFactory,
                                  @Qualifier("PostbankAuthenticationMeansProducerV1") DeutscheBankGroupAuthenticationMeansProducer authenticationMeansProducer,
                                  @Qualifier("PostbankAuthorizationService") DeutscheBankGroupAuthorizationService authorizationService,
                                  @Qualifier("PostbankFetchDataService") DeutscheBankGroupFetchDataService fetchDataService) {
        super(httpClientFactory, authorizationService, fetchDataService, authenticationMeansProducer);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public String getProviderIdentifier() {
        return "POSTBANK";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Postbank";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }
}
