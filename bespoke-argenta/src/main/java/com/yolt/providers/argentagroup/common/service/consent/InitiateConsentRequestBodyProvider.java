package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.dto.CreateConsentRequest;
import com.yolt.providers.argentagroup.dto.CreateConsentRequestAccess;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class InitiateConsentRequestBodyProvider {

    private static final String ACCOUNTS_ACCESS_VALUE = "allAccounts";

    private final Clock clock;

    public CreateConsentRequest provideRequestBody() {
        var createConsentRequestAccess = new CreateConsentRequestAccess();
        createConsentRequestAccess.setAllPsd2(ACCOUNTS_ACCESS_VALUE);

        CreateConsentRequest createConsentRequest = new CreateConsentRequest();
        createConsentRequest.setAccess(createConsentRequestAccess);
        createConsentRequest.setFrequencyPerDay(BigDecimal.valueOf(4));
        createConsentRequest.setValidUntil(LocalDate.now(clock).plusDays(89).format(DateTimeFormatter.ISO_LOCAL_DATE));
        createConsentRequest.setRecurringIndicator(Boolean.TRUE);

        return createConsentRequest;
    }
}
