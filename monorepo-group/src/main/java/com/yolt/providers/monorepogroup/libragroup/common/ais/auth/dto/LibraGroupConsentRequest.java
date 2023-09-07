package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import lombok.Getter;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
public class LibraGroupConsentRequest implements Serializable {

    private static final int CONSENT_VALIDITY_IN_DAYS = 90;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LibraGroupConsentAccess access;
    private final boolean recurringIndicator;
    private final Integer frequencyPerDay;
    private final Boolean combinedServiceIndicator;
    private final String validUntil;

    public LibraGroupConsentRequest(Clock clock) {
        this.recurringIndicator = true;
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
        this.access = new LibraGroupConsentAccess();
        this.validUntil = LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER);
    }

    @Getter
    public class LibraGroupConsentAccess implements Serializable {

        private final String allPsd2;

        public LibraGroupConsentAccess() {
            this.allPsd2 = "allAccounts";
        }
    }
}
