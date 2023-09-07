package com.yolt.providers.fineco.dto;

import com.yolt.providers.common.pis.common.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProviderState {
    private String paymentId;
    private PaymentType paymentType;
}
