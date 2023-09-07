package com.yolt.providers.redsys.common.service;

import lombok.AllArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Objects;

/**
 * Limits, if needed, transaction fetch to given amount of time taking into consideration consent window duration
 */
@AllArgsConstructor
public class TransactionsFetchStartTimeConsentWindowLimiter implements TransactionsFetchStartTime {
    private final Duration consentWindowDuration;
    private final Period fetchWindow;
    private final Clock clock;

    @Override
    public Instant calculate(Instant consentAt, Instant requestedFetchStartTime) {
        final Instant limited = Instant.now(clock).minus(fetchWindow);

        if(limited.isBefore(requestedFetchStartTime)){
            return requestedFetchStartTime;
        }

        if (Objects.isNull(consentAt))
            return limited;

        return Instant.now(clock).isAfter(consentAt.plus(consentWindowDuration)) ? limited : requestedFetchStartTime;
    }
}
