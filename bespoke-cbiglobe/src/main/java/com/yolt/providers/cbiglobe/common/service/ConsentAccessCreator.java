package com.yolt.providers.cbiglobe.common.service;

import com.yolt.providers.cbiglobe.dto.ConsentsAccess;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

public interface ConsentAccessCreator {

    public ConsentsAccess createConsentAccess(ProviderAccountDTO accountToConsent);
}
