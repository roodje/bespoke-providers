package com.yolt.providers.unicredit.common.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class UniCreditSepaPaymentAmountDTO {
    private String currency;
    private String amount;
}
