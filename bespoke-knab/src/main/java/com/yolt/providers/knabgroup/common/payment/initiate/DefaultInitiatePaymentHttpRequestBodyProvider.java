package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<InitiatePaymentPreExecutionResult, InitiatePaymentRequestBody> {

    @Override
    public InitiatePaymentRequestBody provideHttpRequestBody(final InitiatePaymentPreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getRequestDTO();
        return toSepaCreditTransfer(requestDTO);
    }

    private InitiatePaymentRequestBody toSepaCreditTransfer(final SepaInitiatePaymentRequestDTO request) {
        return new InitiatePaymentRequestBody(
                new InitiatePaymentRequestBody.BankAccount(request.getCreditorAccount()),
                request.getDebtorAccount() != null ? new InitiatePaymentRequestBody.BankAccount(request.getDebtorAccount()) : null,
                new InitiatePaymentRequestBody.InstructedAmount(request.getInstructedAmount()),
                request.getCreditorName(),
                request.getRemittanceInformationUnstructured());
    }
}
