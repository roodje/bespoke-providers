package com.yolt.providers.knabgroup.common.payment.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.exception.ProviderStateSerializationException;
import com.yolt.providers.knabgroup.common.payment.DefaultPisAccessTokenProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.PaymentProviderState;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class DefaultSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<StatusPaymentPreExecutionResult> {

    private final DefaultPisAccessTokenProvider accessMeansProvider;
    private final String providerIdentifier;
    private final ObjectMapper objectMapper;

    @Override
    public StatusPaymentPreExecutionResult map(final SubmitPaymentRequest getStatusRequest) {
        try {
            String paymentId = null;
            PaymentType paymentType = null;
            if (StringUtils.hasText(getStatusRequest.getProviderState())) {
                PaymentProviderState paymentProviderState = objectMapper.readValue(getStatusRequest.getProviderState(), PaymentProviderState.class);
                paymentType = paymentProviderState.getPaymentType();
                paymentId = paymentProviderState.getPaymentId();
            }

            KnabGroupAuthenticationMeans authMeans = KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), providerIdentifier);
            String accessToken = accessMeansProvider.getClientAccessToken(
                    authMeans,
                    getStatusRequest.getRestTemplateManager()
            );

            return new StatusPaymentPreExecutionResult(
                    paymentId,
                    getStatusRequest.getRestTemplateManager(),
                    authMeans,
                    accessToken,
                    getStatusRequest.getSigner(),
                    getStatusRequest.getPsuIpAddress(),
                    paymentType
            );
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot deserialize provider state", e);
        }
    }
}
