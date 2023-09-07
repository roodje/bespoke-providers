package com.yolt.providers.n26.ais.v1;

import com.yolt.providers.n26.common.consentwindow.ThreeMonthsAfterFifteenMinutesConsentWindow;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ConsentWindowTest {

    private final ThreeMonthsAfterFifteenMinutesConsentWindow consentWindow = new ThreeMonthsAfterFifteenMinutesConsentWindow();

    @Test
    void shouldReturnThreeMonthsWhenOutOfConsentWindowAndAskingForMoreThanThreeMonths() {
        //given
        Instant now = Instant.now(Clock.systemUTC());
        Instant requestFullYear = now.minus(365, ChronoUnit.DAYS);
        Long consentGenerated = now.minus(90, ChronoUnit.MINUTES).toEpochMilli();
        //when
        Instant returnedFetchDataWindow = consentWindow.whenFromToFetchData(consentGenerated, requestFullYear, Clock.systemUTC());
        //then
        assertThat(returnedFetchDataWindow)
                .isAfterOrEqualTo(now.minus(89, ChronoUnit.DAYS))
                .isBefore(now.minus(88, ChronoUnit.DAYS));
    }

    @Test
    void shouldReturnWhatRequestedForWhenOutOfConsentWindowAndAskingForLessThanThreeMonths() {
        //given
        Instant now = Instant.now(Clock.systemUTC());
        Instant requestTwoMonths = now.minus(60, ChronoUnit.DAYS);
        Long consentGenerated = now.minus(90, ChronoUnit.MINUTES).toEpochMilli();
        //when
        Instant returnedFetchDataWindow = consentWindow.whenFromToFetchData(consentGenerated, requestTwoMonths, Clock.systemUTC());
        //then
        assertThat(returnedFetchDataWindow).
                isAfterOrEqualTo(now.minus(60, ChronoUnit.DAYS))
                .isBefore(now.minus(59, ChronoUnit.DAYS));
    }

    @Test
    void shouldReturnWhatRequestedForWhenInConsentWindow() {
        //given
        Instant now = Instant.now(Clock.systemUTC());
        Instant requestingDataFrom = now.minus(999, ChronoUnit.DAYS);
        Long consentGenerated = now.minus(5, ChronoUnit.MINUTES).toEpochMilli();
        //when
        Instant returnedFetchDataWindow = consentWindow.whenFromToFetchData(consentGenerated, requestingDataFrom, Clock.systemUTC());
        //then
        assertThat(returnedFetchDataWindow)
                .isAfterOrEqualTo(now.minus(999, ChronoUnit.DAYS))
                .isBefore(now.minus(998, ChronoUnit.DAYS));
    }
}
