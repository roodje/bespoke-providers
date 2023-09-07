package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme;

import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

import java.util.HashMap;
import java.util.Map;

public class CamelcaseSchemeMapper extends DefaultSchemeMapper {

    private final Map<ProviderAccountNumberDTO.Scheme, String> accountSchemeToStrMap;

    public CamelcaseSchemeMapper() {
        accountSchemeToStrMap = new HashMap<>();
        accountSchemeToStrMap.put(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER, "UK.OBIE.SortCodeAccountNumber");
        accountSchemeToStrMap.put(ProviderAccountNumberDTO.Scheme.IBAN, "UK.OBIE.IBAN");
    }

    @Override
    public String mapFromScheme(ProviderAccountNumberDTO account) {
        if (!accountSchemeToStrMap.containsKey(account.getScheme())) {
            throw new IllegalArgumentException("Unsupported account scheme: " + account.getScheme());
        }
        return accountSchemeToStrMap.get(account.getScheme());
    }
}
