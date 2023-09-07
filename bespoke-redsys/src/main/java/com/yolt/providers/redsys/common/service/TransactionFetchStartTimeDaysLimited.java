package com.yolt.providers.redsys.common.service;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Limits, if needed, transaction fetch to given amount of time, no matter when consent has been given
 */
@RequiredArgsConstructor
public class TransactionFetchStartTimeDaysLimited implements TransactionsFetchStartTime {

    private final Duration daysLimit;
    private final Clock clock;

    @Override
    public Instant calculate(Instant consentAt, Instant requestedFetchStartTime) {
        final Instant daysAgo = Instant.now(clock).minus(daysLimit);

        return requestedFetchStartTime.isBefore(daysAgo) ? daysAgo : requestedFetchStartTime;
    }
}
