package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetPriorityCode {

    HIGH("HIGH"),
    NORM("NORM");

    private final String value;
}