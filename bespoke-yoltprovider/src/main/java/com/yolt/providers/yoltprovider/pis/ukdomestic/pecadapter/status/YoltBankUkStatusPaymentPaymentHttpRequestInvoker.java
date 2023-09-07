package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.YoltBankUkSubmitPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankUkStatusPaymentPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, YoltBankUkSubmitPreExecutionResult> {

    private final YoltBankUkDomesticHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, YoltBankUkSubmitPreExecutionResult preExecutuionResult) {
        if (preExecutuionResult.getPaymentType() == null) {
            throw PaymentExecutionTechnicalException.statusFailed(new IllegalStateException("Could not find payment type in providerState during retrieving payment status"));
        }
        String paymentId = preExecutuionResult.getPaymentId().toString();
        return switch (preExecutuionResult.getPaymentType()) {
            case SINGLE -> httpService.getSinglePaymentStatus(httpEntity, paymentId);
            case SCHEDULED -> httpService.getScheduledPaymentStatus(httpEntity, paymentId);
            case PERIODIC -> httpService.getPeriodicPaymentStatus(httpEntity, paymentId);
        };
    }
}
