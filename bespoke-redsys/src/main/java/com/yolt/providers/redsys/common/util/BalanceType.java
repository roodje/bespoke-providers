package com.yolt.providers.redsys.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BalanceType {

    INTERIM_AVAILABLE("interimAvailable"),
    INTERIM_BOOKED("interimBooked"),
    CLOSING_BOOKED("closingBooked"),
    OPENING_BOOKED("openingBooked"),
    EXPECTED("expected"),
    FORWARD_AVAILABLE("forwardAvailable");

    private String balanceType;
}
