package com.yolt.providers.deutschebank.common.service.authorization.consent;

import com.yolt.providers.deutschebank.common.domain.model.consent.Access;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationRequest;

import java.time.LocalDate;

public class DeutscheBankGroupGlobalConsentRequestStrategy implements DeutscheBankGroupConsentRequestStrategy {

    @Override
    public ConsentCreationRequest createConsentRequest(LocalDate validUntil) {
        return ConsentCreationRequest.builder()
                .access(Access.builder()
                        .allPsd2("allAccounts")
                        .build())
                .recurringIndicator(true)
                .validUntil(validUntil.toString())
                .frequencyPerDay(4)
                .combinedServiceIndicator(false)
                .build();
    }
}
