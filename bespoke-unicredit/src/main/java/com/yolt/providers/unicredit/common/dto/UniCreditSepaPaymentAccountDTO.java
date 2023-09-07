package com.yolt.providers.unicredit.common.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class UniCreditSepaPaymentAccountDTO {
    private String iban;
    private String currency;
}
