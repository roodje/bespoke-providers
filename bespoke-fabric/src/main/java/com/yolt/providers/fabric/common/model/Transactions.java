package com.yolt.providers.fabric.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions.booked")
    List<Transaction> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<Transaction> getPendingTransactions();

    interface Transaction {
        @JsonPath("$.bankTransactionCode")
        String getBankTransactionCode();

        @JsonPath("$.bookingDate")
        String getBookingDate();

        @JsonPath("$.endToEndId")
        String getEndToEndId();

        @JsonPath("$.remittanceInformationStructured")
        String getRemittanceInformationStructured();

        @JsonPath("$.remittanceInformationUnstructured")
        String getRemittanceInformationUnstructured();

        @JsonPath("$.transactionAmount")
        Amount getAmount();

        @JsonPath("$.transactionId")
        String getTransactionId();

        @JsonPath("$.valueDate")
        String getValueDate();
    }
}
