package com.yolt.providers.stet.generic.service.pec.authorization;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StetPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> {

    @Override
    public String extractAuthorizationUrl(StetPaymentInitiationResponseDTO responseDTO,
                                          StetInitiatePreExecutionResult preExecutionResult) {
        return responseDTO.getLinks().getConsentApproval().getHref();
    }
}
