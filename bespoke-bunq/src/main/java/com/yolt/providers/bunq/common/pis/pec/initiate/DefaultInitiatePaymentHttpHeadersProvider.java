package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<DefaultInitiatePaymentPreExecutionResult, PaymentServiceProviderDraftPaymentRequest> {

    private final DefaultEndpointUrlProvider urlProvider;
    private final BunqHttpHeaderProducer headerProducer;

    @Override
    @SneakyThrows
    public HttpHeaders provideHttpHeaders(DefaultInitiatePaymentPreExecutionResult preExecutionResult, PaymentServiceProviderDraftPaymentRequest paymentRequestBody) {
        return headerProducer.getSignedHeaders(preExecutionResult.getKeyPair(),
                preExecutionResult.getSessionToken(),
                paymentRequestBody,
                urlProvider.getInitiateDraftPaymentUrl(preExecutionResult.getPsd2UserId()));
    }
}
