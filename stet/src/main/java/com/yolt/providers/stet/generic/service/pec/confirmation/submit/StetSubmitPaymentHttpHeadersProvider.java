package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class StetSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<StetConfirmationPreExecutionResult, StetPaymentConfirmationRequestDTO> {

    private final StetPaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult,
                                          StetPaymentConfirmationRequestDTO requestDTO) {
        return httpHeadersFactory.createPaymentSubmitHttpHeaders(preExecutionResult, requestDTO);
    }
}
