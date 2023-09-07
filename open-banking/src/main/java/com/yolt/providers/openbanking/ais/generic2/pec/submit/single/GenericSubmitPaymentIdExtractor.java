package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;

public class GenericSubmitPaymentIdExtractor implements PaymentIdExtractor<OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> {

    @Override
    public String extractPaymentId(OBWriteDomesticResponse5 obWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        return obWriteDomesticResponse5.getData().getDomesticPaymentId();
    }
}
