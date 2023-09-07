package com.yolt.providers.openbanking.ais.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;

public class OpenBankingTestObjectMapper {

    public static final ObjectMapper INSTANCE;

    static {
        final OpenbankingConfiguration openbankingConfiguration = new OpenbankingConfiguration();
        INSTANCE = openbankingConfiguration.getObjectMapper();
    }
}
