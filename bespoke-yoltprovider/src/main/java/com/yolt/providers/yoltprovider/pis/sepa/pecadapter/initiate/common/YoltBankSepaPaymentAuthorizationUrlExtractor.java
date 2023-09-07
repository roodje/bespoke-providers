package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentResponseDTO;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;

public class YoltBankSepaPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(SepaInitiatePaymentResponse responseDTO, YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {
        return responseDTO.getScaRedirect();
    }
}
