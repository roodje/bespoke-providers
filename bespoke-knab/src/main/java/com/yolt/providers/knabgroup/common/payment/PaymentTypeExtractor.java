package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.pis.common.PaymentType;

@FunctionalInterface
public interface PaymentTypeExtractor<HttpResponseBody, PreExecutionResult> {
    PaymentType extractPaymentType(HttpResponseBody httpResponseBody, PreExecutionResult preExecutionResult);
}
