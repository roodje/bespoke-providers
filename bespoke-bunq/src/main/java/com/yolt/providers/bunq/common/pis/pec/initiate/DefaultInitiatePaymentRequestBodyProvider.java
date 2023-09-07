package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.model.PaymentAmount;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;

public class DefaultInitiatePaymentRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<DefaultInitiatePaymentPreExecutionResult, PaymentServiceProviderDraftPaymentRequest> {
    @Override
    public PaymentServiceProviderDraftPaymentRequest provideHttpRequestBody(DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        var requestDto = preExecutionResult.getRequest();
        var paymentRequest = PaymentServiceProviderDraftPaymentRequest.builder()
                .senderIban(requestDto.getDebtorAccount().getIban())
                .counterpartyIban(requestDto.getCreditorAccount().getIban())
                .counterpartyName(requestDto.getCreditorName())
                .description(requestDto.getRemittanceInformationUnstructured())
                .amount(PaymentAmount.builder()
                        .currency("EUR")
                        .value(requestDto.getInstructedAmount().getAmount().toString()).build())
                .build();
        paymentRequest.validate();
        return paymentRequest;
    }
}
