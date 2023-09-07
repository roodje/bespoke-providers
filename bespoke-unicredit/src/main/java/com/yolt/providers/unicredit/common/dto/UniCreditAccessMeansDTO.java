package com.yolt.providers.unicredit.common.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class UniCreditAccessMeansDTO {

    private final String consentId;
    private final Instant created;
    private final Instant expireTime;
}
