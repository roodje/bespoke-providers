package com.yolt.providers.rabobank.pis.pec.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderState;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderStateDeserializer;
import com.yolt.providers.rabobank.pis.pec.submit.RabobankSepaSubmitPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.fromPISAuthenticationMeans;

@RequiredArgsConstructor
public class RabobankSepaStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<RabobankSepaSubmitPaymentPreExecutionResult> {

    private final RabobankPaymentProviderStateDeserializer providerStateDeserializer;

    @Override
    public RabobankSepaSubmitPaymentPreExecutionResult map(GetStatusRequest getStatusRequest) throws PaymentExecutionTechnicalException {
        RabobankAuthenticationMeans authenticationMeans = fromPISAuthenticationMeans(getStatusRequest.getAuthenticationMeans());
        String paymentId = extractPaymentId(getStatusRequest);
        return new RabobankSepaSubmitPaymentPreExecutionResult(paymentId,
                authenticationMeans,
                getStatusRequest.getRestTemplateManager(),
                getStatusRequest.getPsuIpAddress(),
                getStatusRequest.getSigner());
    }

    private String extractPaymentId(GetStatusRequest getStatusRequest) {
        if (!StringUtils.isEmpty(getStatusRequest.getPaymentId())) {
            return getStatusRequest.getPaymentId();
        }

        RabobankPaymentProviderState providerState = providerStateDeserializer.deserialize(getStatusRequest.getProviderState());
        return providerState.getPaymentId();
    }
}
