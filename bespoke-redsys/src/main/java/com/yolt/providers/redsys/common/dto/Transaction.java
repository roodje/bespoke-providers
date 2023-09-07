package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Transaction {
    private String transactionId;

    private String endToEndId;

    private String entryReference;

    private String mandateId;

    private String checkId;

    private String creditorId;

    private String bookingDate;

    private String valueDate;

    private Amount transactionAmount;

    private List<ReportExchangeRate> currencyExchange;

    private String creditorName;

    private AccountReference creditorAccount;

    private String ultimateCreditor;

    private String debtorName;

    private AccountReference debtorAccount;

    private String ultimateDebtor;

    private String remittanceInformationUnstructured;

    private String remittanceInformationStructured;

    private String purposeCode;

    private String bankTransactionCode;

    private String proprietaryBankTransactionCode;

    @JsonProperty("_links")
    private Links links;
}
