package com.yolt.providers.n26.common.consentwindow;

import java.time.Clock;
import java.time.Instant;

public interface ConsentWindow {
    Instant whenFromToFetchData(Long consentGeneratedAt, Instant fetchDataStartTime, Clock clock);
}
