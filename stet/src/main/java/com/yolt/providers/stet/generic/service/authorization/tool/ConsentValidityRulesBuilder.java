package com.yolt.providers.stet.generic.service.authorization.tool;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsentValidityRulesBuilder {

    private final Set<String> consentPageKeywords = new HashSet<>();

    public static ConsentValidityRulesBuilder consentPageRules() {
        return new ConsentValidityRulesBuilder();
    }

    public static ConsentValidityRules emptyRules() {
        return consentPageRules().build();
    }

    public ConsentValidityRulesBuilder containsKeyword(final String keyword) {
        consentPageKeywords.add(keyword);
        return this;
    }

    public ConsentValidityRules build() {
        return new ConsentValidityRules(consentPageKeywords);
    }
}
