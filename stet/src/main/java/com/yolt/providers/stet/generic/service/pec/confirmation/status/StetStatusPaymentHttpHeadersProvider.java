package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class StetStatusPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<StetConfirmationPreExecutionResult, StetPaymentConfirmationRequestDTO> {

    private final StetPaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult,
                                          StetPaymentConfirmationRequestDTO unused) {
        return httpHeadersFactory.createPaymentStatusHttpHeaders(preExecutionResult);
    }
}
