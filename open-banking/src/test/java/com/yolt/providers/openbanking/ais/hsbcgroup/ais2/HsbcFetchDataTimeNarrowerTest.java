package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.HsbcFetchDataTimeNarrower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class HsbcFetchDataTimeNarrowerTest {

    private Instant fromFetchDateBefore89days;
    private Instant fromFetchDateAfter89days;
    private Instant fromFetchDateMaxTime;

    private HsbcFetchDataTimeNarrower narrower = new HsbcFetchDataTimeNarrower(Clock.systemUTC());

    @BeforeEach
    void setup() {
        fromFetchDateMaxTime = Instant.now().minus(Period.ofDays(89)).minus(Period.ofDays(1));
        fromFetchDateAfter89days = Instant.now().minus(Period.ofDays(88));
        fromFetchDateBefore89days = Instant.now().minus(Period.ofDays(90));
    }

    @Test
    void shouldNotNarrowFetchDataStartTimeWhenShortAndInConsentWindow() {
        //given
        Instant fetchDataStartTime = Instant.now();

        // when
        Instant narrowedFetchDataStartTime = narrower.narrowTransactionFetchStartTime(fetchDataStartTime, true);

        // then
        assertThat(narrowedFetchDataStartTime).isEqualTo(fetchDataStartTime);
    }

    @Test
    void shouldNotNarrowFetchDataStartTimeWhenIn90DaysAndInConsentWindow() {
        //given
        Instant fetchDataStartTime = fromFetchDateAfter89days;

        // when
        Instant narrowedFetchDataStartTime = narrower.narrowTransactionFetchStartTime(fetchDataStartTime, true);

        // then
        assertThat(narrowedFetchDataStartTime).isEqualTo(fetchDataStartTime);
    }

    @Test
    void shouldNotNarrowFetchDataStartTimeWhenIn90DaysAndIOutOfConsentWindow() {
        //given
        Instant fetchDataStartTime = fromFetchDateAfter89days;

        // when
        Instant narrowedFetchDataStartTime = narrower.narrowTransactionFetchStartTime(fetchDataStartTime, false);

        // then
        assertThat(narrowedFetchDataStartTime).isEqualTo(fetchDataStartTime);
    }

    @Test
    void shouldNotNarrowFetchDataStartTimeWhenNotIn90DaysAndInConsentWindow() {
        //given
        Instant fetchDataStartTime = fromFetchDateBefore89days;

        // when
        Instant narrowedFetchDataStartTime = narrower.narrowTransactionFetchStartTime(fetchDataStartTime, true);

        // then
        assertThat(narrowedFetchDataStartTime).isEqualTo(fetchDataStartTime);
    }

    @Test
    void shouldNarrowFetchDataStartTimeWhenNotIn90DaysAndOutOfConsentWindow() {
        //given
        Instant fetchDataStartTime = fromFetchDateBefore89days;

        // when
        Instant narrowedFetchDataStartTime = narrower.narrowTransactionFetchStartTime(fetchDataStartTime, false);

        // then
        assertThat(narrowedFetchDataStartTime.isAfter(fromFetchDateMaxTime)).isTrue();
    }
}