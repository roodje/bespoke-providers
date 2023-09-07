package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import org.apache.commons.lang3.StringUtils;

public class GenericScheduledPaymentStatusResourceIdExtractor implements PaymentIdExtractor<ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult> {

    @Override
    public String extractPaymentId(ScheduledPaymentStatusResponse paymentStatusResponse, GenericPaymentStatusPreExecutionResult preExecutionResult) {
        return StringUtils.isEmpty(preExecutionResult.getPaymentId()) ? "" : preExecutionResult.getPaymentId();
    }
}
