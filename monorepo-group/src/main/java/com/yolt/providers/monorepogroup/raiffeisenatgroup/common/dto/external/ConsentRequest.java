package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class ConsentRequest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Access access;
    private final boolean recurringIndicator;
    private final String validUntil;
    private final int frequencyPerDay;

    public ConsentRequest(LocalDate validUntil, int frequencyPerDay) {
        this.access = new Access();
        this.recurringIndicator = true;
        this.validUntil = validUntil.format(DATE_TIME_FORMATTER);
        this.frequencyPerDay = frequencyPerDay;

    }

    @Data
    private class Access {
        private final String availableAccounts = "allAccounts";
    }
}
