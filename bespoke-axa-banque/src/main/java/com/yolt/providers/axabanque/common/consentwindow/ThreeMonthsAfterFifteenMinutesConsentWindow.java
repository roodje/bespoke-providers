package com.yolt.providers.axabanque.common.consentwindow;

import lombok.AllArgsConstructor;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

@AllArgsConstructor
public class ThreeMonthsAfterFifteenMinutesConsentWindow implements ConsentWindow {

    private final Clock clock;

    @Override
    public Instant whenFromToFetchData(Long consentGeneratedAt, Instant fetchDataStartTime) {
        return Instant.ofEpochMilli(consentGeneratedAt).plus(15, MINUTES).isBefore(Instant.now(clock)) ?
                        upToThreeMonths(fetchDataStartTime) : fetchDataStartTime;
    }

    private Instant upToThreeMonths(Instant fetchDataStartTime) {
        Instant threeMonthsAgo = Instant.now(clock).minus(90, DAYS);
        return fetchDataStartTime.isBefore(threeMonthsAgo)
                ? threeMonthsAgo : fetchDataStartTime;
    }
}
