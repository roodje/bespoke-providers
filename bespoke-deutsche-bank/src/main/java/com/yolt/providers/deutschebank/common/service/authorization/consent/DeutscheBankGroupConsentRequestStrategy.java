package com.yolt.providers.deutschebank.common.service.authorization.consent;

import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationRequest;

import java.time.LocalDate;

public interface DeutscheBankGroupConsentRequestStrategy {

    ConsentCreationRequest createConsentRequest(LocalDate validUntil);
}
