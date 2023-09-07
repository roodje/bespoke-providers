package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderState;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderStateDeserializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VolksbankSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<VolksbankSepaSubmitPreExecutionResult> {

    private final VolksbankPaymentProviderStateDeserializer providerStateDeserializer;
    private final ProviderIdentification providerIdentification;

    @Override
    public VolksbankSepaSubmitPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        var authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        var restTemplateManager = submitPaymentRequest.getRestTemplateManager();
        VolksbankPaymentProviderState providerState = providerStateDeserializer.deserialize(submitPaymentRequest.getProviderState());

        return VolksbankSepaSubmitPreExecutionResult.builder()
                .authenticationMeans(authenticationMeans)
                .restTemplateManager(restTemplateManager)
                .paymentId(providerState.getPaymentId())
                .build();
    }
}
