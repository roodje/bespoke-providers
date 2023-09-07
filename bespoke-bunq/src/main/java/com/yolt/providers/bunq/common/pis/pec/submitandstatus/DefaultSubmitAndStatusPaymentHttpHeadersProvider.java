package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class DefaultSubmitAndStatusPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<DefaultSubmitAndStatusPaymentPreExecutionResult, Void> {

    private final DefaultEndpointUrlProvider urlProvider;
    private final BunqHttpHeaderProducer headerProducer;

    @Override
    @SneakyThrows
    public HttpHeaders provideHttpHeaders(DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult, Void paymentRequestBody) {
        return headerProducer.getSignedHeaders(preExecutionResult.getKeyPair(),
                preExecutionResult.getSessionToken(),
                new byte[]{},
                urlProvider.getStatusDraftPaymentUrl(preExecutionResult.getPsd2UserId(), preExecutionResult.getPaymentId()));
    }
}
