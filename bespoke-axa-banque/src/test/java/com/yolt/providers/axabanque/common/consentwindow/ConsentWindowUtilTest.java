package com.yolt.providers.axabanque.common.consentwindow;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsentWindowUtilTest {

    private final ThreeMonthsAfterFifteenMinutesConsentWindow consentWindow = new ThreeMonthsAfterFifteenMinutesConsentWindow(Clock.systemUTC());

    @Test
    public void shouldReturnThreeMonthsFromNowWhenOutOfConsentWindowAndRequestingForMore() {
        //given
        Instant now = Instant.now();
        Instant requestingDataFrom =  now.minus(365, DAYS);
        long consentGenerated25minAgo =  now.minus(25, MINUTES).toEpochMilli();
        //when
        Instant instant = consentWindow.whenFromToFetchData(consentGenerated25minAgo, requestingDataFrom);
        //then
        assertThat(instant).isAfterOrEqualTo(now.minus(90,DAYS));
        assertThat(instant).isBefore(now.minus(89,DAYS));
    }

    @Test
    public void shouldReturnWhatRequestedForWhenOutOfConsentWindowAndAskingForLessThanThreeMonths() {
        //given
        Instant now = Instant.now();
        Instant requestingDataFrom = now.minus(30, DAYS);
        long consentGenerated25minAgo =  now.minus(25, MINUTES).toEpochMilli();
        //when
        Instant instant = consentWindow.whenFromToFetchData(consentGenerated25minAgo, requestingDataFrom);
        //then
        assertThat(instant).isAfterOrEqualTo(now.minus(30,DAYS));
        assertThat(instant).isBefore(now.minus(29,DAYS));
    }

    @Test
    public void shouldReturnWhatRequestedForWhenInConsentWindow() {
        //given
        Instant now = Instant.now();
        Instant requestingDataFrom =  now.minus(999, DAYS);
        long consentGenerated5minAgo =  now.minus(5, MINUTES).toEpochMilli();
        //when
        Instant instant = consentWindow.whenFromToFetchData(consentGenerated5minAgo, requestingDataFrom);
        //then
        assertThat(instant).isAfterOrEqualTo(now.minus(999,DAYS));
        assertThat(instant).isBefore(now.minus(998,DAYS));
    }
}
