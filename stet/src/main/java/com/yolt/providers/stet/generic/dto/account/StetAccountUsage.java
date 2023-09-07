package com.yolt.providers.stet.generic.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Known in STET as UsageEnum
 */
@Getter
@AllArgsConstructor
public enum StetAccountUsage {
    PRIV("PRIV"),
    ORGA("ORGA");

    private final String value;
}