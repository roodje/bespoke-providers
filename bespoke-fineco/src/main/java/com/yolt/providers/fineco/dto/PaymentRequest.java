package com.yolt.providers.fineco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private String endToEndIdentification;
    private CreditorDebtorAccount creditorAccount;
    private CreditorDebtorAccount debtorAccount;
    private InstructedAmount instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;

}
