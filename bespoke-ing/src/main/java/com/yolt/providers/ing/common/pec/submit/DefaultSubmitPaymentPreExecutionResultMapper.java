package com.yolt.providers.ing.common.pec.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.dto.PaymentProviderState;
import com.yolt.providers.ing.common.pec.DefaultPisAccessMeansProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Clock;

@RequiredArgsConstructor
public class DefaultSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<DefaultSubmitPaymentPreExecutionResult> {

    private final DefaultPisAccessMeansProvider accessTokenProvider;
    private final ObjectMapper objectMapper;
    private final String providerIdentifier;
    private final Clock clock;

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public DefaultSubmitPaymentPreExecutionResult map(final SubmitPaymentRequest submitPaymentRequest) {
        IngAuthenticationMeans authMeans = IngAuthenticationMeans.createIngAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans(), providerIdentifier);
        IngClientAccessMeans accessMeans = accessTokenProvider.getClientAccessMeans(
                authMeans,
                submitPaymentRequest.getRestTemplateManager(),
                submitPaymentRequest.getSigner(),
                clock
        );

        PaymentProviderState providerState = objectMapper.readValue(submitPaymentRequest.getProviderState(), PaymentProviderState.class);

        return new DefaultSubmitPaymentPreExecutionResult(
                providerState.getPaymentId(),
                submitPaymentRequest.getRestTemplateManager(),
                authMeans,
                accessMeans,
                submitPaymentRequest.getSigner(),
                submitPaymentRequest.getPsuIpAddress(),
                providerState.getPaymentType()
        );
    }
}
