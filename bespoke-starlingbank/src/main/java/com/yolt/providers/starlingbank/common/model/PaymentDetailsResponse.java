package com.yolt.providers.starlingbank.common.model;

import lombok.Data;

@Data
public class PaymentDetailsResponse {
    private String paymentUid;
    private CurrencyAndAmountV2 amount;
    private String reference;
    private String payeeUid;
    private String payeeAccountUid;
    private String createdAt;
    private String completedAt;
    private String rejectedAt;
    private PaymentStatusDetails paymentStatusDetails;
}
