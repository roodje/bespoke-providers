package com.yolt.providers.n26.common.consentwindow;

import org.springframework.util.ObjectUtils;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

public class ThreeMonthsAfterFifteenMinutesConsentWindow implements ConsentWindow {

    @Override
    public Instant whenFromToFetchData(Long consentGeneratedAt, Instant fetchDataStartTime, final Clock clock) {
        if (ObjectUtils.isEmpty(consentGeneratedAt)) {
            return upToThreeMonths(fetchDataStartTime, clock);
        }
        return Instant.ofEpochMilli(consentGeneratedAt).plus(15, MINUTES).isBefore(Instant.now(clock)) ?
                upToThreeMonths(fetchDataStartTime, clock) : fetchDataStartTime;
    }

    private static Instant upToThreeMonths(Instant fetchDataStartTime, Clock clock) {
        Instant threeMonthsAgo = Instant.now(clock).minus(89, DAYS);
        return fetchDataStartTime.isBefore(threeMonthsAgo)
                ? threeMonthsAgo : fetchDataStartTime;
    }
}
