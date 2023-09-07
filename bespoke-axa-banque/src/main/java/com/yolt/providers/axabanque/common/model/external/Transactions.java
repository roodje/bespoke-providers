package com.yolt.providers.axabanque.common.model.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions._links")
    TransactionsMetaData getTransactionsMetaData();

    interface TransactionsMetaData {
        @JsonPath("$.first.href")
        String getFirst();

        @JsonPath("$.previous.href")
        String getPrevious();

        @JsonPath("$.next.href")
        String getNext();

        @JsonPath("$.last.href")
        String getLast();
    }

    @JsonPath("$.transactions.booked")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<Transaction> getPendingTransactions();

    interface Transaction {
        @JsonPath("$.bankTransactionCode")
        String getBankTransactionCode();

        @JsonPath("$.bookingDate")
        String getBookingDate();

        @JsonPath("$.creditorAccount")
        Account getCreditorAccount();

        @JsonPath("$.creditorId")
        String getCreditorId();

        @JsonPath("$.creditorName")
        String getCreditorName();

        @JsonPath("$.debtorAccount")
        Account getDebtorAccount();

        @JsonPath("$.debtorName")
        String getDebtorName();

        @JsonPath("$.endToEndId")
        String getEndToEndId();

        @JsonPath("$.mandateId")
        String getMandateId();

        @JsonPath("$.purposeCode")
        String getPurposeCode();

        @JsonPath("$.remittanceInformationStructured")
        String getRemittanceInformationStructured();

        @JsonPath("$.remittanceInformationUnstructured")
        String getRemittanceInformationUnstructured();

        @JsonPath("$.transactionAmount")
        Amount getAmount();

        @JsonPath("$.transactionId")
        String getTransactionId();

        @JsonPath("$.ultimateCreditor")
        String getUltimateCreditor();

        @JsonPath("$.ultimateDebtor")
        String getUltimateDebtor();

        @JsonPath("$.valueDate")
        String getValueDate();
    }
}
