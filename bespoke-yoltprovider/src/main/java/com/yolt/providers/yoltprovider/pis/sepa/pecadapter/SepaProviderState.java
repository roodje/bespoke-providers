package com.yolt.providers.yoltprovider.pis.sepa.pecadapter;

import com.yolt.providers.common.pis.common.PaymentType;
import lombok.Value;

@Value
public class SepaProviderState {

    String paymentId;
    PaymentType paymentType;
}
