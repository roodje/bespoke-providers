package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;

@RequiredArgsConstructor
public class HsbcFetchDataTimeNarrower {

    private final Clock clock;

    public Instant narrowTransactionFetchStartTime(Instant transactionFetchStartTime, boolean isInConsentWindow) {
        final Instant maxOutOffConsentWindowFetchStartTime = Instant.now(clock).minus(Period.ofDays(89));
        if (!isInConsentWindow && transactionFetchStartTime.isBefore(maxOutOffConsentWindowFetchStartTime)) {
            return maxOutOffConsentWindowFetchStartTime;
        }
        return transactionFetchStartTime;
    }
}
