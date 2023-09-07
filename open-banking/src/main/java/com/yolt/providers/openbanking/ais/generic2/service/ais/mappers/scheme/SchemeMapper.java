package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme;

import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

public interface SchemeMapper {

    String mapFromScheme(ProviderAccountNumberDTO account);

    ProviderAccountNumberDTO.Scheme mapToScheme(String schemeName);
}
