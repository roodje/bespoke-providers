package com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;

import java.util.EnumMap;
import java.util.Map;

public class CamelCaseUkSchemeMapper implements UkSchemeMapper {

    private final Map<AccountIdentifierScheme, String> accountSchemeToStrMap;

    public CamelCaseUkSchemeMapper() {
        accountSchemeToStrMap = new EnumMap<>(AccountIdentifierScheme.class);
        accountSchemeToStrMap.put(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, "UK.OBIE.SortCodeAccountNumber");
        accountSchemeToStrMap.put(AccountIdentifierScheme.IBAN, "UK.OBIE.IBAN");
    }

    @Override
    public String map(AccountIdentifierScheme scheme) {
        if (!accountSchemeToStrMap.containsKey(scheme)) {
            throw new IllegalArgumentException("Unsupported account scheme: " + scheme);
        }
        return accountSchemeToStrMap.get(scheme);
    }
}
