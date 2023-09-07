package com.yolt.providers.bancatransilvania.bancatransilvania;

import com.yolt.providers.bancatransilvania.common.BancaTransilvaniaGroupDataProvider;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducer;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClientFactory;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupAuthorizationService;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupFetchDataService;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupRegistrationService;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BancaTransilvaniaDataProviderV1 extends BancaTransilvaniaGroupDataProvider {

    public BancaTransilvaniaDataProviderV1(@Qualifier("BancaTransilvaniaHttpClientFactory") BancaTransilvaniaGroupHttpClientFactory httpClientFactory,
                                           @Qualifier("BancaTransilvaniaAuthenticationMeansProducerV1") BancaTransilvaniaGroupAuthenticationMeansProducer authenticationMeansProducer,
                                           @Qualifier("BancaTransilvaniaAuthorizationService") BancaTransilvaniaGroupAuthorizationService authorizationService,
                                           @Qualifier("BancaTransilvaniaRegistrationService") BancaTransilvaniaGroupRegistrationService registrationService,
                                           @Qualifier("BancaTransilvaniaFetchDataService") BancaTransilvaniaGroupFetchDataService fetchDataService) {
        super(httpClientFactory, registrationService, authorizationService, fetchDataService, authenticationMeansProducer);
    }

    @Override
    public String getProviderIdentifier() {
        return "BANCA_TRANSILVANIA";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Banca Transilvania";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }
}
