package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.consentvalidity;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;

import java.util.Collections;
import java.util.function.Supplier;

public class LloydsBankingGroupConsentValidityRulesSupplier implements Supplier<ConsentValidityRules> {
    @Override
    public ConsentValidityRules get() {
        return new ConsentValidityRules(Collections.singleton("UPDATE THIS COPY"));
    }
}
