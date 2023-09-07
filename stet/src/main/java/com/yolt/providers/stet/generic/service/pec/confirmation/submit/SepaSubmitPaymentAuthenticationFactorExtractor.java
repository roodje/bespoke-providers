package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

public interface SepaSubmitPaymentAuthenticationFactorExtractor<PreExecutionResult> {

    String extractAuthenticationFactor(PreExecutionResult preExecutionResult);
}
