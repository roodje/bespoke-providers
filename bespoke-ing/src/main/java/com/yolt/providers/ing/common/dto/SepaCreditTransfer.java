package com.yolt.providers.ing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SepaCreditTransfer {

    private String endToEndIdentification;
    private CreditorAccount creditorAccount;
    private DebtorAccount debtorAccount;
    private InstructedAmount instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private String requestedExecutionDate;
    private String startDate;
    private String endDate;
    private String frequency;
}
