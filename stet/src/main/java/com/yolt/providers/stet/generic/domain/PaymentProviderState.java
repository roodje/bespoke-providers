package com.yolt.providers.stet.generic.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class PaymentProviderState {

    private String paymentId;

    public static PaymentProviderState initiatedProviderState(String paymentId) {
        return new PaymentProviderState(paymentId);
    }

    private PaymentProviderState(String paymentId) {
        this.paymentId = paymentId;
    }
}
