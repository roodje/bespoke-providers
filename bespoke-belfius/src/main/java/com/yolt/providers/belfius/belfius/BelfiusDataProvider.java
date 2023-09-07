package com.yolt.providers.belfius.belfius;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.belfius.config.BelfiusProperties;
import com.yolt.providers.belfius.belfius.service.BelfiusAuthorizationService;
import com.yolt.providers.belfius.belfius.service.BelfiusFetchService;
import com.yolt.providers.belfius.common.BelfiusGroupDataProvider;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Service
public class BelfiusDataProvider extends BelfiusGroupDataProvider {

    public BelfiusDataProvider(BelfiusAuthorizationService authorizationService,
                               BelfiusFetchService fetchService,
                               BelfiusProperties properties,
                               Clock clock,
                               @Qualifier("BelfiusObjectMapper") ObjectMapper objectMapper) {
        super(objectMapper, properties, authorizationService, fetchService, clock);
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public String getProviderIdentifier() {
        return "BELFIUS";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Belfius";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }
}
