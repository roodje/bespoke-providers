package com.yolt.providers.cbiglobe.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentRequest {

    private String endToEndIdentification;
    private CreditorDebtorAccount creditorAccount;
    private CreditorDebtorAccount debtorAccount;
    private InstructedAmount instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private final String transactionType = "remote_transaction";

}
