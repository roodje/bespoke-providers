package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

public class StetSubmitPaymentAuthenticationFactorExtractor implements SepaSubmitPaymentAuthenticationFactorExtractor<StetConfirmationPreExecutionResult> {

    private final Supplier<String> authenticationFactorQueryParameterSupplier;

    public StetSubmitPaymentAuthenticationFactorExtractor() {
        this.authenticationFactorQueryParameterSupplier = () -> "psuAuthenticationFactor";
    }

    @Override
    public String extractAuthenticationFactor(StetConfirmationPreExecutionResult preExecutionResult) {
        return UriComponentsBuilder.fromUriString(preExecutionResult.getRedirectUrlPostedBackFromSite())
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(authenticationFactorQueryParameterSupplier.get());
    }
}
