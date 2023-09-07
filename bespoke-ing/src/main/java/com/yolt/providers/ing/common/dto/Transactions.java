package com.yolt.providers.ing.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions.booked")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<Transaction> getPendingTransactions();

    @JsonPath("$.transactions._links.next.href")
    String getNextPageUrl();

    interface Transaction {

        @JsonPath("$.transactionId")
        String getTransactionId();

        @JsonPath("$.valueDate")
        String getValueDate();

        @JsonPath("$.transactionAmount.amount")
        BigDecimal getAmount();

        @JsonPath("$.remittanceInformationUnstructured")
        String getDescription();

        @JsonPath("$.creditorName")
        String getCreditorName();

        @JsonPath("$.endToEndId")
        String getEndToEndId();

        @JsonPath("$.bookingDate")
        String getBookingDate();

        @JsonPath("$.transactionAmount.currency")
        String getCurrency();

        @JsonPath("$.debtorName")
        String getDebtorName();

        @JsonPath("$.creditorAccount")
        TargetAccount getCreditorAccount();

        @JsonPath("$.debtorAccount")
        TargetAccount getDebtorAccount();

        @JsonPath("$.remittanceInformationStructured")
        RemittanceInformationStructured getRemittanceInformationStructured();

        @JsonPath("$.remittanceInformationUnstructured")
        String getRemittanceInformationUnstructured();

        @JsonPath("$.executionDateTime")
        String getExecutionDateTime();

        @JsonPath("$.transactionType")
        String getTransactionType();
    }

    interface TargetAccount {

        @JsonPath("$.iban")
        String getIban();

        @JsonPath("$.bban")
        String getBban();

        @JsonPath("$.bic")
        String getBic();
    }

    interface RemittanceInformationStructured {

        @JsonPath("$.type")
        String getType();

        @JsonPath("$.issuer")
        String getIssuer();

        @JsonPath("$.reference")
        String getReference();
    }
}
