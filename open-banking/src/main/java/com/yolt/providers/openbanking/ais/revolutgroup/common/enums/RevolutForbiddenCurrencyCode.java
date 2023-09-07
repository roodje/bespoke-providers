package com.yolt.providers.openbanking.ais.revolutgroup.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RevolutForbiddenCurrencyCode {
    BTC,
    LTC,
    ETH,
    BCH,
    XRP,
    XLM,
    // C4PO-8608
    OMG,
    OGN

}
