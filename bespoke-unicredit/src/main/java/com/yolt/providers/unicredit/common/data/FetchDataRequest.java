package com.yolt.providers.unicredit.common.data;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FetchDataRequest {
    String consentId;
    String psuIpAddress;
    Instant fetchStartTime;
}
