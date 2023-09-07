package com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;

public class DefaultUkSchemeMapper implements UkSchemeMapper {

    private static final String OB_3_1_6_SCHEME_PREFIX = "UK.OBIE.";

    @Override
    public String map(AccountIdentifierScheme scheme) {
        return OB_3_1_6_SCHEME_PREFIX + scheme.toString();
    }
}
