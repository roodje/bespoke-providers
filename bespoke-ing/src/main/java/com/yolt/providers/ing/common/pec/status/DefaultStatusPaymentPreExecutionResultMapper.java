package com.yolt.providers.ing.common.pec.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.dto.PaymentProviderState;
import com.yolt.providers.ing.common.exception.ProviderStateDeserializationException;
import com.yolt.providers.ing.common.pec.DefaultPisAccessMeansProvider;
import com.yolt.providers.ing.common.pec.submit.DefaultSubmitPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.Clock;

@RequiredArgsConstructor
public class DefaultStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<DefaultSubmitPaymentPreExecutionResult> {

    private final DefaultPisAccessMeansProvider accessMeansProvider;
    private final String providerIdentifier;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public DefaultSubmitPaymentPreExecutionResult map(final GetStatusRequest getStatusRequest) {
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

            IngAuthenticationMeans authMeans = IngAuthenticationMeans.createIngAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), providerIdentifier);
            IngClientAccessMeans accessMeans = accessMeansProvider.getClientAccessMeans(
                    authMeans,
                    getStatusRequest.getRestTemplateManager(),
                    getStatusRequest.getSigner(),
                    clock
            );

            return new DefaultSubmitPaymentPreExecutionResult(
                    paymentId,
                    getStatusRequest.getRestTemplateManager(),
                    authMeans,
                    accessMeans,
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
