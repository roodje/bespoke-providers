package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CbiGlobePaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<PaymentInitiationRequestResponseType, CbiGlobeSepaInitiatePreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(PaymentInitiationRequestResponseType initiatePaymentResponse, CbiGlobeSepaInitiatePreExecutionResult preExecutionResult) {
        return initiatePaymentResponse.getLinks().getScaRedirect().getHref();
    }
}
