package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;

public class YoltBankUkSubmitPaymentIdExtractor implements PaymentIdExtractor<PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult> {

    @Override
    public String extractPaymentId(PaymentSubmitResponse obWriteDomesticResponse1, YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult) {
        return obWriteDomesticResponse1.getData().getPaymentId();
    }
}
