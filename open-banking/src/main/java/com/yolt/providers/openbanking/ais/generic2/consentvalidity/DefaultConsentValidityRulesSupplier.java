package com.yolt.providers.openbanking.ais.generic2.consentvalidity;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;

import java.util.function.Supplier;

public class DefaultConsentValidityRulesSupplier implements Supplier<ConsentValidityRules> {
    @Override
    public ConsentValidityRules get() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
