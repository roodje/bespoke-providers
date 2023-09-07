package com.yolt.providers.stet.lclgroup.common.pec.confirmation;

import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.SepaSubmitPaymentAuthenticationFactorExtractor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class LclGroupSubmitPaymentAuthenticationFactorExtractor implements SepaSubmitPaymentAuthenticationFactorExtractor<StetConfirmationPreExecutionResult> {

    @Override
    public String extractAuthenticationFactor(StetConfirmationPreExecutionResult stetConfirmationPreExecutionResult) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
