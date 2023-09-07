package com.yolt.providers.stet.labanquepostalegroup.common.service.pec.submit;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.SepaSubmitPaymentAuthenticationFactorExtractor;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.StetSubmitPaymentHttpRequestBodyProvider;
import org.springframework.util.StringUtils;

public class LaBanquePostaleGroupStetSubmitPaymentHttpRequestBodyProvider extends StetSubmitPaymentHttpRequestBodyProvider {
    public LaBanquePostaleGroupStetSubmitPaymentHttpRequestBodyProvider(SepaSubmitPaymentAuthenticationFactorExtractor<StetConfirmationPreExecutionResult> authenticationFactorExtractor) {
        super(authenticationFactorExtractor);
    }

    @Override
    public StetPaymentConfirmationRequestDTO provideHttpRequestBody(StetConfirmationPreExecutionResult preExecutionResult) {
        String psuAuthenticationFactor = authenticationFactorExtractor.extractAuthenticationFactor(preExecutionResult);
        if (!StringUtils.hasText(psuAuthenticationFactor)) {
            throw new IllegalStateException("PSU Authentication Factor value is missing");
        }
        return StetPaymentConfirmationRequestDTO.builder()
                .psuAuthenticationFactor(psuAuthenticationFactor)
                .build();
    }
}
