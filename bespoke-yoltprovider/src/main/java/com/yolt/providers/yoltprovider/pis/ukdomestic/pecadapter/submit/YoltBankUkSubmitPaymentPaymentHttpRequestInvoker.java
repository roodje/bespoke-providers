package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.ukdomestic.ConfirmPaymentRequest;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class YoltBankUkSubmitPaymentPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<ConfirmPaymentRequest, YoltBankUkSubmitPreExecutionResult> {

    private final YoltBankUkDomesticHttpService httpService;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<ConfirmPaymentRequest> httpEntity, YoltBankUkSubmitPreExecutionResult result) {
        if (result.getPaymentType() == null) {
            throw PaymentExecutionTechnicalException.paymentSubmissionException(new IllegalStateException("Could not find payment type in providerState during payment submission"));
        }
        return switch (result.getPaymentType()) {
            case SINGLE -> httpService.postSubmitSinglePayment(httpEntity);
            case SCHEDULED -> httpService.postSubmitScheduledPayment(httpEntity);
            case PERIODIC -> httpService.postSubmitPeriodicPayment(httpEntity);
        };

    }
}
