package com.yolt.providers.n26.n26.config;

import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.n26.common.N26GroupDataProvider;
import com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeansProducer;
import com.yolt.providers.n26.common.http.N26GroupHttpClientFactory;
import com.yolt.providers.n26.common.service.N26GroupAuthorizationService;
import com.yolt.providers.n26.common.service.N26GroupFetchDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class N26DataProviderV1 extends N26GroupDataProvider {

    public N26DataProviderV1(@Qualifier("N26HttpClientFactory") N26GroupHttpClientFactory httpClientFactory,
                             @Qualifier("N26AuthorizationServiceV1") N26GroupAuthorizationService authorizationService,
                             @Qualifier("N26FetchDataServiceV1") N26GroupFetchDataService fetchDataService,
                             @Qualifier("N26AuthenticationMeansProducerV1") N26GroupAuthenticationMeansProducer authenticationMeansProducer) {
        super(httpClientFactory, authorizationService, fetchDataService, authenticationMeansProducer);
    }

    @Override
    public String getProviderIdentifier() {
        return "N26";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "N26";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }
}
