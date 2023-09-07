package com.yolt.providers.knabgroup.common.dto.internal;

import lombok.Data;

import java.util.List;

@Data
public class ConsentRequest {

    private static final int FREQUENCY_PER_DAY = 4;
    private static final List<String> ACCESS_LIST = List.of("accounts", "transactions", "balances");

    private final List<String> access;
    private final boolean recurringIndicator;
    private final String validUntil;
    private final int frequencyPerDay;
    private final boolean combinedServiceIndicator;

    public ConsentRequest(final String validUntil) {
        this.access = ACCESS_LIST;
        this.frequencyPerDay = FREQUENCY_PER_DAY;
        this.recurringIndicator = true;
        this.combinedServiceIndicator = false;
        this.validUntil = validUntil;
    }

}