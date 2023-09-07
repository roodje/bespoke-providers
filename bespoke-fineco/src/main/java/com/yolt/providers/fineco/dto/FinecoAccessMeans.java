package com.yolt.providers.fineco.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class FinecoAccessMeans {
    private final String consentId;
    private final Instant consentCreateTime;
    private final Instant consentExpireTime;
}
