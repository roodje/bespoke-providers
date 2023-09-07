package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme;

import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

public class DefaultSchemeMapper implements SchemeMapper {
    private static final String OB_3_1_6_SCHEME_PREFIX = "UK.OBIE.";

    @Override
    public String mapFromScheme(ProviderAccountNumberDTO account) {
        return OB_3_1_6_SCHEME_PREFIX + account.getScheme().toString();
    }

    @Override
    public ProviderAccountNumberDTO.Scheme mapToScheme(String schemeName) {
        String value = schemeName.toUpperCase().replace(OB_3_1_6_SCHEME_PREFIX, "");
        try {
            return ProviderAccountNumberDTO.Scheme.valueOf(value);
        } catch (Exception ignore) {
            return null;
        }
    }
}
