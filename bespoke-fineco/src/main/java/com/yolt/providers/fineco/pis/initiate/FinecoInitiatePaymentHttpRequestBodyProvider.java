package com.yolt.providers.fineco.pis.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.fineco.dto.CreditorDebtorAccount;
import com.yolt.providers.fineco.dto.InstructedAmount;
import com.yolt.providers.fineco.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

@RequiredArgsConstructor
public class FinecoInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<FinecoInitiatePaymentPreExecutionResult, PaymentRequest> {

    @Override
    public PaymentRequest provideHttpRequestBody(FinecoInitiatePaymentPreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO request = preExecutionResult.getRequestDTO();
        PaymentRequest paymentRequest = new PaymentRequest();
        if (request.getDebtorAccount() != null) {
            paymentRequest.setDebtorAccount(toCreditorDebtorAccount(request.getDebtorAccount()));
        }
        paymentRequest.setCreditorAccount(toCreditorDebtorAccount(request.getCreditorAccount()));
        paymentRequest.setCreditorName(request.getCreditorName());
        paymentRequest.setEndToEndIdentification(request.getEndToEndIdentification());
        paymentRequest.setInstructedAmount(toInstructedAmount(request.getInstructedAmount()));
        paymentRequest.setRemittanceInformationUnstructured(request.getRemittanceInformationUnstructured());
        return paymentRequest;
    }

    private InstructedAmount toInstructedAmount(final SepaAmountDTO sepaAmountDTO) {
        InstructedAmount instructedAmount = new InstructedAmount();
        instructedAmount.setAmount(sepaAmountDTO.getAmount().toPlainString());
        instructedAmount.setCurrency(CurrencyCode.EUR.name());
        return instructedAmount;
    }

    private CreditorDebtorAccount toCreditorDebtorAccount(final SepaAccountDTO sepaAccountDTO) {
        CreditorDebtorAccount account = new CreditorDebtorAccount();
        account.setIban(sepaAccountDTO.getIban());
        return account;
    }
}
