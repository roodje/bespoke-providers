package com.yolt.providers.fabric.common.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class ConsentRequest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final boolean combinedServiceIndicator;
    private final Access access;
    private final int frequencyPerDay;
    private final boolean recurringIndicator;
    private final String validUntil;

    public ConsentRequest(final LocalDate validUntil, final int frequencyPerDay) {
        combinedServiceIndicator  = false;
        this.access = new Access();
        this.frequencyPerDay = frequencyPerDay;
        this.validUntil = validUntil.format(FORMATTER);
        this.recurringIndicator = true;
    }

    @Data
    private static class Access {
        private String allPsd2 = "allAccounts";
    }
}
