package com.yolt.providers.axabanque.common.model.internal;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class ConsentDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final boolean combinedServiceIndicator;
    private final Access access;
    private final int frequencyPerDay;
    private final boolean recurringIndicator;
    private final String validUntil;

    public ConsentDTO(final LocalDate validUntil, final int frequencyPerDay) {
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
