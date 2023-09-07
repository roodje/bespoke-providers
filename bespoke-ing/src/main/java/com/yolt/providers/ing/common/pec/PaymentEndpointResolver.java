package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.pis.common.PaymentType;

import static com.yolt.providers.common.pis.common.PaymentType.PERIODIC;
import static com.yolt.providers.ing.common.pec.IngPecConstants.*;

public class PaymentEndpointResolver {

    public String getInitiatePaymentEndpoint(PaymentType paymentType) {
        if (PERIODIC.equals(paymentType)) {
            return INITIATE_PERIODIC_PAYMENT_ENDPOINT;
        }

        return INITIATE_PAYMENT_ENDPOINT;
    }

    public String getSubmitPaymentEndpoint(PaymentType paymentType) {
        if (PERIODIC.equals(paymentType)) {
            return SUBMIT_PERIODIC_PAYMENT_ENDPOINT;
        }

        return SUBMIT_PAYMENT_ENDPOINT;
    }

    public String getStatusPaymentEndpoint(PaymentType paymentType) {
        if (PERIODIC.equals(paymentType)) {
            return STATUS_PERIODIC_PAYMENT_ENDPOINT;
        }

        return STATUS_PAYMENT_ENDPOINT;
    }
}
