package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;

public class YoltBankSepaSubmitPaymentIdExtractor implements PaymentIdExtractor<SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> {

    @Override
    public String extractPaymentId(SepaPaymentStatusResponse sepaPaymentStatusResponseDTO, YoltBankSepaSubmitPreExecutionResult yoltBankSepaSubmitPreExecutionResult) {
        return sepaPaymentStatusResponseDTO.getPaymentId();
    }
}
