package com.yolt.providers.triodosbank.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private String transactionId;
    private String bookingDate;
    private String valueDate;
    private AmountType transactionAmount;
    private String debtorName;
    private Account debtorAccount;
    private String creditorName;
    private Account creditorAccount;
    private String remittanceInformationUnstructured;
    private String proprietaryBankTransactionCode;
}
