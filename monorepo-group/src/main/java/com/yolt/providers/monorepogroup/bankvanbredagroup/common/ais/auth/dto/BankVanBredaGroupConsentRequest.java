package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.Getter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
public class BankVanBredaGroupConsentRequest {

    private static final int CONSENT_VALIDITY_IN_DAYS = 90;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BankVanBredaGroupConsentRequest(Clock clock) {
        this.validUntil = LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER);
    }

    private final BankVanBredaGroupConsentAccess access = new BankVanBredaGroupConsentAccess();
    private final boolean recurringIndicator = true;
    private final Integer frequencyPerDay = 4;
    private final Boolean combinedServiceIndicator = false;
    private final String validUntil;

    @Getter
    private class BankVanBredaGroupConsentAccess {
        private String allPsd2 = "allAccounts";
    }

}

