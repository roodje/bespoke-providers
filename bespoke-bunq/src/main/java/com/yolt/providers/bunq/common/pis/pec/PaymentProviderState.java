package com.yolt.providers.bunq.common.pis.pec;

import com.yolt.providers.common.pis.common.PaymentType;
import lombok.Value;

@Value
public class PaymentProviderState {

    private int paymentId;
    private PaymentType paymentType;
    private String sessionToken;
    private long expirationTime;
    private String keyPairPublic;
    private String keyPairPrivate;
}
