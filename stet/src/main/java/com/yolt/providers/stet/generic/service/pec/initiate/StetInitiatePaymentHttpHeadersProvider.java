package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class StetInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<StetInitiatePreExecutionResult, StetPaymentInitiationRequestDTO> {

    private final StetPaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(StetInitiatePreExecutionResult preExecutionResult,
                                          StetPaymentInitiationRequestDTO requestDTO) {
        return httpHeadersFactory.createPaymentInitiationHttpHeaders(preExecutionResult, requestDTO);
    }
}
