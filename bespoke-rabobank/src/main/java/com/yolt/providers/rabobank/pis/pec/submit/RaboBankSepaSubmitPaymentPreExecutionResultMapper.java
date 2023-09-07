package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankPaymentProviderStateDeserializer;
import lombok.RequiredArgsConstructor;

import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.fromPISAuthenticationMeans;

@RequiredArgsConstructor
public class RaboBankSepaSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<RabobankSepaSubmitPaymentPreExecutionResult> {

    private final RabobankPaymentProviderStateDeserializer providerStateDeserializer;

    @Override
    public RabobankSepaSubmitPaymentPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        String paymentId = providerStateDeserializer.deserialize(submitPaymentRequest.getProviderState()).getPaymentId();
        RabobankAuthenticationMeans authenticationMeans = fromPISAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans());
        verifyRedirectSuccessful(submitPaymentRequest.getRedirectUrlPostedBackFromSite());
        return new RabobankSepaSubmitPaymentPreExecutionResult(paymentId,
                authenticationMeans,
                submitPaymentRequest.getRestTemplateManager(),
                submitPaymentRequest.getPsuIpAddress(),
                submitPaymentRequest.getSigner());
    }

    private void verifyRedirectSuccessful(String redirectUrlPostedBackFromSite) {
        if (redirectUrlPostedBackFromSite.contains("error")) {
            throw new IllegalStateException("Got error in callback URL. Payment confirmation failed. Redirect url: " + redirectUrlPostedBackFromSite);
        }
    }
}
