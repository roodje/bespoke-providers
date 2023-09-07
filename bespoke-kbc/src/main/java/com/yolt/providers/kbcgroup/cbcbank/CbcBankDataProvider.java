package com.yolt.providers.kbcgroup.cbcbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.kbcgroup.common.KbcGroupDataProvider;
import com.yolt.providers.kbcgroup.common.service.KbcGroupAuthenticationService;
import com.yolt.providers.kbcgroup.common.service.KbcGroupFetchDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Service
public class CbcBankDataProvider extends KbcGroupDataProvider {

    public CbcBankDataProvider(@Qualifier("CbcBankAuthenticationService") KbcGroupAuthenticationService kbcGroupAuthenticationService,
                               @Qualifier("CbcBankFetchDataService") KbcGroupFetchDataService kbcGroupFetchDataService,
                               @Qualifier("KbcGroupObjectMapper") ObjectMapper objectMapper,
                               Clock clock) {
        super(kbcGroupAuthenticationService, kbcGroupFetchDataService, objectMapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "CBC_BANK";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "CBC Bank";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }
}
