package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpService;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.YoltBankSepaSubmitPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankSepaStatusPaymentPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<Void, YoltBankSepaSubmitPreExecutionResult> {

    private final YoltBankSepaPaymentHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<Void> httpEntity, YoltBankSepaSubmitPreExecutionResult preExecutionResult) {
        if (preExecutionResult.getPaymentType() == null) {
            throw PaymentExecutionTechnicalException.statusFailed(new IllegalStateException("Could not find payment type in providerState during retrieving payment status"));
        }
        return switch (preExecutionResult.getPaymentType()) {
            case SCHEDULED, SINGLE -> httpService.getSingleStatus(httpEntity, preExecutionResult.getPaymentId());
            case PERIODIC -> httpService.getPeriodicStatus(httpEntity, preExecutionResult.getPaymentId());
        };
    }

}
