package com.yolt.providers.fineco.pis.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import com.yolt.providers.fineco.dto.PaymentProviderState;
import com.yolt.providers.fineco.exception.ProviderStateDeserializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class FinecoStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<FinecoStatusPaymentPreExecutionResult> {

    private final String providerIdentifier;
    private final ObjectMapper objectMapper;

    @Override
    public FinecoStatusPaymentPreExecutionResult map(GetStatusRequest getStatusRequest) {
        try {
            String paymentId = getStatusRequest.getPaymentId();
            PaymentType paymentType = null;
            if (StringUtils.hasText(getStatusRequest.getProviderState())) {
                PaymentProviderState paymentProviderState = objectMapper.readValue(getStatusRequest.getProviderState(), PaymentProviderState.class);
                paymentType = paymentProviderState.getPaymentType();
                if (!StringUtils.hasText(paymentId)) {
                    paymentId = paymentProviderState.getPaymentId();
                }
            }

            FinecoAuthenticationMeans authenticationMeans = FinecoAuthenticationMeans.fromAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), providerIdentifier);

            return new FinecoStatusPaymentPreExecutionResult(
                    paymentId,
                    getStatusRequest.getRestTemplateManager(),
                    authenticationMeans,
                    getStatusRequest.getSigner(),
                    getStatusRequest.getPsuIpAddress(),
                    paymentType
            );
        }
        catch (JsonProcessingException e) {
            throw new ProviderStateDeserializationException("Cannot deserialize provider state", e);
        }
    }
}
