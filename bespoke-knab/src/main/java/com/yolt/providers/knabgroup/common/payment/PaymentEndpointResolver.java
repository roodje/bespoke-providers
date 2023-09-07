package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.pis.common.PaymentType;

public class PaymentEndpointResolver {
    public static final String INITIATE_PAYMENT_ENDPOINT = "/v1/payments/instant-sepa-credit-transfers";
    public static final String STATUS_PAYMENT_ENDPOINT = "/v1/payments/instant-sepa-credit-transfers/{id}";

    public String getInitiatePaymentEndpoint(PaymentType paymentType) {
        return INITIATE_PAYMENT_ENDPOINT;
    }

    public String getStatusPaymentEndpoint(PaymentType paymentType) {
        return STATUS_PAYMENT_ENDPOINT;
    }
}
