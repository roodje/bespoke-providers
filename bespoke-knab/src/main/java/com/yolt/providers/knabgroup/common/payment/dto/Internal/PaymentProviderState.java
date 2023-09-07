package com.yolt.providers.knabgroup.common.payment.dto.Internal;

import com.yolt.providers.common.pis.common.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentProviderState {
    private final String paymentId;
    private final PaymentType paymentType;
}
