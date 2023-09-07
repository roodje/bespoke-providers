package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.model.CreditorDebtorAccount;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.model.InstructedAmount;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class CbiGlobeInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<CbiGlobeSepaInitiatePreExecutionResult, InitiatePaymentRequest> {

    private final CbiGlobeAccountToCurrencyMapper cbiGlobeAccountToCurrencyMapper;
    private final CbiGlobeInstructedAmountToCurrencyMapper cbiGlobeInstructedAmountToCurrencyMapper;

    @Override
    public InitiatePaymentRequest provideHttpRequestBody(CbiGlobeSepaInitiatePreExecutionResult preExecutionResult) {
        var requestDTO = preExecutionResult.getRequestDTO();

        var paymentRequest = new InitiatePaymentRequest();
        if (!ObjectUtils.isEmpty(requestDTO.getDebtorAccount())) {
            paymentRequest.setDebtorAccount(toCreditorDebtorAccount(requestDTO.getDebtorAccount()));
        }
        paymentRequest.setCreditorAccount(toCreditorDebtorAccount(requestDTO.getCreditorAccount()));
        paymentRequest.setCreditorName(requestDTO.getCreditorName());
        paymentRequest.setEndToEndIdentification(requestDTO.getEndToEndIdentification());
        paymentRequest.setInstructedAmount(toInstructedAmount(requestDTO));
        paymentRequest.setRemittanceInformationUnstructured(requestDTO.getRemittanceInformationUnstructured());
        return paymentRequest;
    }

    protected CreditorDebtorAccount toCreditorDebtorAccount(SepaAccountDTO sepaAccountDTO) {
        var account = new CreditorDebtorAccount();
        account.setCurrency(cbiGlobeAccountToCurrencyMapper.map(sepaAccountDTO));
        account.setIban(sepaAccountDTO.getIban());
        return account;
    }

    protected InstructedAmount toInstructedAmount(SepaInitiatePaymentRequestDTO request) {
        var instructedAmount = new InstructedAmount();
        instructedAmount.setAmount(request.getInstructedAmount().getAmount().toPlainString());
        instructedAmount.setCurrency(cbiGlobeInstructedAmountToCurrencyMapper.map(request));
        return instructedAmount;
    }
}
