package com.yolt.providers.yoltprovider.pis.ukdomestic;

import lombok.Value;

import java.util.UUID;

@Value
public class ConfirmPaymentRequest {
    private UUID paymentId;
}
