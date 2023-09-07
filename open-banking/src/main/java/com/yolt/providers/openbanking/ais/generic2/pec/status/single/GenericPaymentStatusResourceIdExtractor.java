package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import org.apache.commons.lang3.StringUtils;

public class GenericPaymentStatusResourceIdExtractor implements PaymentIdExtractor<PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentStatusResponse paymentStatusResponse, GenericPaymentStatusPreExecutionResult preExecutionResult) {
        return StringUtils.isEmpty(preExecutionResult.getPaymentId()) ? "" : preExecutionResult.getPaymentId();
    }
}
