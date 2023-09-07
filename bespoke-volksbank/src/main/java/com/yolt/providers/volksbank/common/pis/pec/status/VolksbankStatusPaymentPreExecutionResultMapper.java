package com.yolt.providers.volksbank.common.pis.pec.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderState;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPaymentProviderStateDeserializer;
import com.yolt.providers.volksbank.common.pis.pec.submit.VolksbankSepaSubmitPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class VolksbankStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<VolksbankSepaSubmitPreExecutionResult> {

    private final VolksbankPaymentProviderStateDeserializer providerStateDeserializer;
    private final ProviderIdentification providerIdentification;

    @Override
    public VolksbankSepaSubmitPreExecutionResult map(GetStatusRequest getStatusRequest) {
        String paymentId = extractPaymentId(getStatusRequest);
        return VolksbankSepaSubmitPreExecutionResult.builder()
                .authenticationMeans(VolksbankAuthenticationMeans.fromAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier()))
                .restTemplateManager(getStatusRequest.getRestTemplateManager())
                .paymentId(paymentId)
                .build();
    }

    private String extractPaymentId(GetStatusRequest getStatusRequest) {
        if (!StringUtils.isEmpty(getStatusRequest.getPaymentId())) {
            return getStatusRequest.getPaymentId();
        }

        VolksbankPaymentProviderState providerState = providerStateDeserializer.deserialize(getStatusRequest.getProviderState());
        return providerState.getPaymentId();
    }
}
