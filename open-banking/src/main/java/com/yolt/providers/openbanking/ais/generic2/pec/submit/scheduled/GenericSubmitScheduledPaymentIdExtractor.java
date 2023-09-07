package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;

public class GenericSubmitScheduledPaymentIdExtractor implements PaymentIdExtractor<OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        return obWriteDomesticScheduledResponse5.getData().getDomesticScheduledPaymentId();
    }
}
