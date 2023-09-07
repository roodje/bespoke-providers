package com.yolt.providers.redsys.common.rest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum BookingStatus {
    BOTH("both"),
    BOOKED("booked");

    private final String status;
}
