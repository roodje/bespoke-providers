package com.yolt.providers.axabanque.common.consentwindow;

import java.time.Instant;

public interface ConsentWindow {
    Instant whenFromToFetchData(Long consentGeneratedAt, Instant fetchDataStartTime);
}
