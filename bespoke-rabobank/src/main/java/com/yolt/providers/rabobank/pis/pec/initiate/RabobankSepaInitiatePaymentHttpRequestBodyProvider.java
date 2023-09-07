package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.rabobank.dto.external.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.springframework.util.ObjectUtils;

public class RabobankSepaInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<RabobankSepaInitiatePreExecutionResult, SepaCreditTransfer> {

    @Override
    public SepaCreditTransfer provideHttpRequestBody(RabobankSepaInitiatePreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getRequestDTO();
        CreditorAddress creditorAddress = new CreditorAddress();
        creditorAddress.setCountry(requestDTO.getDynamicFields().getCreditorPostalCountry());
        SepaCreditTransfer sepaCreditTransfer = new SepaCreditTransfer();
        sepaCreditTransfer.setCreditorAccount(mapToCreditorAccount(requestDTO.getCreditorAccount()));
        sepaCreditTransfer.setCreditorAddress(creditorAddress);
        sepaCreditTransfer.setCreditorName(requestDTO.getCreditorName());
        sepaCreditTransfer.setDebtorAccount(mapToDebtorAccount(requestDTO.getDebtorAccount()));
        sepaCreditTransfer.setEndToEndIdentification(requestDTO.getEndToEndIdentification());
        sepaCreditTransfer.setInstructedAmount(mapToInstructedAmount(requestDTO.getInstructedAmount()));
        sepaCreditTransfer.setRemittanceInformationUnstructured(requestDTO.getRemittanceInformationUnstructured());
        return sepaCreditTransfer;
    }

    private InstructedAmount mapToInstructedAmount(final SepaAmountDTO sepaAmountDTO) {
        InstructedAmount instructedAmount = new InstructedAmount();
        instructedAmount.setContent(sepaAmountDTO.getAmount().toPlainString());
        instructedAmount.setCurrency(CurrencyCode.EUR.name());
        return instructedAmount;
    }

    private DebtorAccount mapToDebtorAccount(final SepaAccountDTO sepaAccountDTO) {
        if (ObjectUtils.isEmpty(sepaAccountDTO)) {
            return null;
        }
        DebtorAccount debtorAccount = new DebtorAccount();
        debtorAccount.setCurrency(sepaAccountDTO.getCurrency().name());
        debtorAccount.setIban(sepaAccountDTO.getIban());
        return debtorAccount;
    }

    private CreditorAccount mapToCreditorAccount(final SepaAccountDTO sepaAccountDTO) {
        CreditorAccount creditorAccount = new CreditorAccount();
        creditorAccount.setCurrency(sepaAccountDTO.getCurrency().name());
        creditorAccount.setIban(sepaAccountDTO.getIban());
        return creditorAccount;
    }
}
