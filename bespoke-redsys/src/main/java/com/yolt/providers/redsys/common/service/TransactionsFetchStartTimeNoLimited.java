package com.yolt.providers.redsys.common.service;

import java.time.Instant;

/**
 * Default implementation without limiting transaction start time after consent window.
 */
public class TransactionsFetchStartTimeNoLimited implements TransactionsFetchStartTime {
    @Override
    public Instant calculate(Instant consentAt, Instant requestedFetchStartTime) {
        return requestedFetchStartTime;
    }
}
