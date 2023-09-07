package com.yolt.providers.redsys.common.service;

import java.time.Instant;

/**
 * TransactionsFetchStartTime is a policy to limit transaction fetch start time.
 */
public interface TransactionsFetchStartTime {
    /**
     * @param consentAt timestamp when consent was generated
     * @param requestedFetchStartTime time requested by Site-Management
     * @return original or limited transaction fetch start time
     */
    Instant calculate(Instant consentAt, Instant requestedFetchStartTime);
}
