package com.yolt.providers.deutschebank.common.service.authorization.consent;

import com.yolt.providers.deutschebank.common.domain.model.consent.Access;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationRequest;

import java.time.LocalDate;
import java.util.Collections;

public class DeutscheBankGroupDrivenConsentRequestStrategy implements DeutscheBankGroupConsentRequestStrategy {

    @Override
    public ConsentCreationRequest createConsentRequest(LocalDate validUntil) {
        return ConsentCreationRequest.builder()
                .access(Access.builder()
                        .balances(Collections.emptyList())
                        .transactions(Collections.emptyList())
                        .build())
                .recurringIndicator(true)
                .validUntil(validUntil.toString())
                .frequencyPerDay(4)
                .combinedServiceIndicator(false)
                .build();
    }
}
