package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.volksbank.dto.v1_1.AccountReference;
import com.yolt.providers.volksbank.dto.v1_1.Amount;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest;

public class VolksbankInitiatePaymentHttpRequestBodyProviderV2 implements PaymentExecutionHttpRequestBodyProvider<VolksbankSepaInitiatePreExecutionResult, InitiatePaymentRequest> {

    @Override
    public InitiatePaymentRequest provideHttpRequestBody(VolksbankSepaInitiatePreExecutionResult preExecutionResult) {
        var requestDTO = preExecutionResult.getRequestDTO();

        var result = new InitiatePaymentRequest();
        var instructedAmount = new Amount();
        instructedAmount.setAmount(requestDTO.getInstructedAmount().getAmount().toString());
        instructedAmount.setCurrency(Amount.CurrencyEnum.EUR);
        result.setInstructedAmount(instructedAmount);

        if (requestDTO.getDebtorAccount() != null) {
            var debtorAccount = new AccountReference();
            debtorAccount.setCurrency(AccountReference.CurrencyEnum.fromValue(requestDTO.getDebtorAccount().getCurrency().name()));
            debtorAccount.setIban(requestDTO.getDebtorAccount().getIban());
            result.setDebtorAccount(debtorAccount);
        }

        var creditorAccount = new AccountReference();
        creditorAccount.setCurrency(AccountReference.CurrencyEnum.fromValue(requestDTO.getCreditorAccount().getCurrency().name()));
        creditorAccount.setIban(requestDTO.getCreditorAccount().getIban());
        result.setCreditorAccount(creditorAccount);

        result.setCreditorName(requestDTO.getCreditorName());
        result.setRemittanceInformationUnstructured(requestDTO.getRemittanceInformationUnstructured());

        return result;
    }
}
