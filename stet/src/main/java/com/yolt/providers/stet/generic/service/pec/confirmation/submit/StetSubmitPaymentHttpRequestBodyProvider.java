package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StetSubmitPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<StetConfirmationPreExecutionResult, StetPaymentConfirmationRequestDTO> {

    protected final SepaSubmitPaymentAuthenticationFactorExtractor<StetConfirmationPreExecutionResult> authenticationFactorExtractor;

    @Override
    public StetPaymentConfirmationRequestDTO provideHttpRequestBody(StetConfirmationPreExecutionResult preExecutionResult) {
        String psuAuthenticationFactor = authenticationFactorExtractor.extractAuthenticationFactor(preExecutionResult);
        return StetPaymentConfirmationRequestDTO.builder()
                .psuAuthenticationFactor(psuAuthenticationFactor)
                .build();
    }
}
