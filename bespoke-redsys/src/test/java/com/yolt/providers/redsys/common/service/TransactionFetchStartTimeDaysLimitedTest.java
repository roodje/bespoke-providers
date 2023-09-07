package com.yolt.providers.redsys.common.service;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * This test class verifies logic to set transaction fetching time without consent window.
 * Covered flows:
 * - use TransactionsFetchStartTime value from site-management when date is in allowed limit
 * - limit TransactionsFetchStartTime to maximum enable value, when date is not in allowed limit
 * <p>
 */
class TransactionFetchStartTimeDaysLimitedTest {

    private static final Duration DAYS_LIMIT = Duration.ofDays(89L);

    private TransactionFetchStartTimeDaysLimited sut = new TransactionFetchStartTimeDaysLimited(DAYS_LIMIT, Clock.systemUTC());

    private Clock clock = Clock.systemUTC();

    @Test
    void shouldNotLimitFetchStartTimeForCalculateWithFetchStartTimeNotExceedingLimit() {
        // given
        Instant fetchStartTime = Instant.now(clock).minus(DAYS_LIMIT.minusDays(10));

        // when
        Instant result = sut.calculate(Instant.now(clock), fetchStartTime);

        // then
        assertThat(result)
                .isEqualTo(fetchStartTime);
    }

    @Test
    void shouldLimitFetchStartTimeForCalculateWithFetchStartTimeExceedingLimit() {
        // given
        Instant fetchStartTime = Instant.now(clock).minus(DAYS_LIMIT.plusDays(1));

        // when
        Instant result = sut.calculate(Instant.now(clock), fetchStartTime);

        // then
        assertThat(result)
                .isCloseTo(Instant.now(clock).minus(DAYS_LIMIT), within(1, ChronoUnit.MINUTES));
    }
}
