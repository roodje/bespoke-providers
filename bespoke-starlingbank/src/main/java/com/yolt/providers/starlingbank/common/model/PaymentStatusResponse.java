package com.yolt.providers.starlingbank.common.model;

import lombok.Data;

import java.util.List;

@Data
public class PaymentStatusResponse {
    private List<PaymentDetailsResponse> payments;
}
