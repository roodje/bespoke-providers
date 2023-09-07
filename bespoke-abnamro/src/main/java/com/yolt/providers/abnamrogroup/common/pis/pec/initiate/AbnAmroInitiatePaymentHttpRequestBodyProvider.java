package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;

public class AbnAmroInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<AbnAmroInitiatePaymentPreExecutionResult, SepaPayment> {

    @Override
    public SepaPayment provideHttpRequestBody(AbnAmroInitiatePaymentPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        var requestDTO = preExecutionResult.getRequestDTO();
        var sepaPayment = new SepaPayment();
        if (requestDTO.getDebtorAccount() != null) {
            sepaPayment.setInitiatingpartyAccountNumber(requestDTO.getDebtorAccount().getIban());
        }
        sepaPayment.setCounterpartyAccountNumber(requestDTO.getCreditorAccount().getIban());
        sepaPayment.amount(requestDTO.getInstructedAmount().getAmount().floatValue());
        sepaPayment.counterpartyName(requestDTO.getCreditorName());
        sepaPayment.remittanceInfo(requestDTO.getRemittanceInformationUnstructured());
        return sepaPayment;
    }
}
