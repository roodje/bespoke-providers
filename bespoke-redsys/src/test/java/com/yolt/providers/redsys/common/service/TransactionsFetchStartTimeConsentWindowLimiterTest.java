package com.yolt.providers.redsys.common.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * This test class verifies logic to set transaction fetching time in consent window.
 * Covered flows:
 * - use TransactionsFetchStartTime value from site-management when date is in allowed limit
 * - limit TransactionsFetchStartTime to maximum enable value, when date is not in allowed limit
 * <p>
 */
class TransactionsFetchStartTimeConsentWindowLimiterTest {

    private static final Clock CLOCK = Clock.systemUTC();
    private static final Instant FETCH_START_DAYS_AGO_200 = Instant.now(CLOCK).minus(Duration.ofDays(200)).truncatedTo(ChronoUnit.HOURS);
    private static final Instant FETCH_START_DAYS_AGO_89 = Instant.now(CLOCK).minus(Duration.ofDays(89)).truncatedTo(ChronoUnit.HOURS);
    private static final Instant CONSENT_MINUTES_AGO_5 = Instant.now(CLOCK).minus(Duration.ofMinutes(5));
    private static final Instant CONSENT_MINUTES_AGO_30 = Instant.now(CLOCK).minus(Duration.ofMinutes(30));

    private TransactionsFetchStartTimeConsentWindowLimiter transactionsFetchStartTime;

    @Test
    void shouldNotLimitInConsentWindow() {
        // given
        transactionsFetchStartTime = new TransactionsFetchStartTimeConsentWindowLimiter(Duration.ofMinutes(20), Period.ofDays(89), Clock.systemUTC());
        // when
        final Instant limited = transactionsFetchStartTime.calculate(CONSENT_MINUTES_AGO_5, FETCH_START_DAYS_AGO_200);
        // then
        assertThat(limited.truncatedTo(ChronoUnit.HOURS))
                .isCloseTo(FETCH_START_DAYS_AGO_200, within(1, ChronoUnit.MINUTES));
    }

    @Test
    void shouldLimitAfterConsentWindow() {
        // given
        transactionsFetchStartTime = new TransactionsFetchStartTimeConsentWindowLimiter(Duration.ofMinutes(20), Period.ofDays(89), Clock.systemUTC());
        // when
        final Instant limited = transactionsFetchStartTime.calculate(CONSENT_MINUTES_AGO_30, FETCH_START_DAYS_AGO_200);
        // then
        assertThat(limited.truncatedTo(ChronoUnit.HOURS))
                .isCloseTo(FETCH_START_DAYS_AGO_89, within(1, ChronoUnit.MINUTES));

    }

    @Test
    void shouldNotExtendFetchStartTime() {
        // given
        transactionsFetchStartTime = new TransactionsFetchStartTimeConsentWindowLimiter(Duration.ofMinutes(20), Period.ofDays(100), Clock.systemUTC());
        // when
        final Instant limited = transactionsFetchStartTime.calculate(CONSENT_MINUTES_AGO_30, FETCH_START_DAYS_AGO_89);
        // then
        assertThat(limited.truncatedTo(ChronoUnit.HOURS))
                .isCloseTo(FETCH_START_DAYS_AGO_89, within(1, ChronoUnit.MINUTES));

    }

}
