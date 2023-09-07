package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;

@FunctionalInterface
public interface SepaTokenPaymentHttpRequestInvoker<PreExecutionResult> {

    TokenResponseDTO invokeRequest(PreExecutionResult preExecutionResult);
}
