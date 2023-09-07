package com.yolt.providers.unicredit.common.dto;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class UniCreditInitiateSepaPaymentRequestDTO {
    private UniCreditSepaPaymentAccountDTO debtorAccount;
    private UniCreditSepaPaymentAmountDTO instructedAmount;
    private UniCreditSepaPaymentAccountDTO creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private String endToEndIdentification;

    public static UniCreditInitiateSepaPaymentRequestDTO from(SepaInitiatePaymentRequestDTO sepaInitiatePaymentRequestDTO) {
        SepaAccountDTO debtorAccount = sepaInitiatePaymentRequestDTO.getDebtorAccount();
        SepaAmountDTO instructedAmount = sepaInitiatePaymentRequestDTO.getInstructedAmount();
        SepaAccountDTO creditorAccount = sepaInitiatePaymentRequestDTO.getCreditorAccount();
        String creditorName = sepaInitiatePaymentRequestDTO.getCreditorName();
        String remittanceInformationUnstructured = sepaInitiatePaymentRequestDTO.getRemittanceInformationUnstructured();
        String endToEndIdentification = sepaInitiatePaymentRequestDTO.getEndToEndIdentification();
        return builder()
                .debtorAccount(UniCreditSepaPaymentAccountDTO.builder()
                        .iban(debtorAccount != null ? debtorAccount.getIban() : "")
                        .currency(debtorAccount != null && debtorAccount.getCurrency() != null ? debtorAccount.getCurrency().name() : "EUR")
                        .build())
                .instructedAmount(UniCreditSepaPaymentAmountDTO.builder()
                        .amount(instructedAmount != null && instructedAmount.getAmount() != null ? instructedAmount.getAmount().toString() : "")
                        .currency("EUR")
                        .build())
                .creditorAccount(UniCreditSepaPaymentAccountDTO.builder()
                        .iban(creditorAccount != null ? creditorAccount.getIban() : "")
                        .currency(creditorAccount != null && creditorAccount.getCurrency() != null ? creditorAccount.getCurrency().name() : "EUR")
                        .build())
                .creditorName(creditorName != null ? creditorName : "")
                .remittanceInformationUnstructured(remittanceInformationUnstructured != null ? remittanceInformationUnstructured : "")
                .endToEndIdentification(endToEndIdentification != null ? endToEndIdentification : "")
                .build();
    }
}
