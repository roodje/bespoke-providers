package com.yolt.providers.direkt1822group.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String transactionId;
    private Amount transactionAmount;
    private AccountReference creditorAccount;
    private AccountReference debtorAccount;
    private String bookingDate;
    private String valueDate;
    private String creditorName;
    private String debtorName;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String entryReference;
    private String endToEndId;
    private String mandateId;
    private String creditorId;
    private String proprietaryBankTransactionCode;
}
