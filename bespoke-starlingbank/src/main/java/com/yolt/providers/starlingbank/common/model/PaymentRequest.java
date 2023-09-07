package com.yolt.providers.starlingbank.common.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    private String externalIdentifier;
    private PaymentRecipient paymentRecipient;
    private String reference;
    private CurrencyAndAmountV2 amount;
}
