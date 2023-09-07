package com.yolt.providers.ing.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TestTransaction implements Transactions.Transaction {
    public String transactionId;
    public String valueDate;
    public BigDecimal amount;
    public String description;
    public String creditorName;
    public String endToEndId;
    public String bookingDate;
    public String currency;
    public String debtorName;
    public Transactions.TargetAccount creditorAccount;
    public Transactions.TargetAccount debtorAccount;
    public Transactions.RemittanceInformationStructured remittanceInformationStructured;
    public String remittanceInformationUnstructured;
    public String executionDateTime;
    public String transactionType;
}
